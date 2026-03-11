package com.progresssoft.fxdeals.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.progresssoft.fxdeals.dto.ImportResult;
import com.progresssoft.fxdeals.entity.Deal;
import com.progresssoft.fxdeals.exception.BadRequestException;
import com.progresssoft.fxdeals.repository.DealRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;

/**
 * Unit tests for {@link DealImportService}.
 *
 * <p>These tests focus on import accounting and fault isolation because those are the
 * highest-risk areas for CSV ingestion regressions.</p>
 */
class DealImportServiceTest {

    @Mock
    private DealRepository dealRepository;

    private DealImportService dealImportService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        dealImportService = new DealImportService(dealRepository, validator);
    }

    @Test
    void shouldIgnoreHeaderRowWhenPresent() {
        // The happy path should tolerate CSVs exported with headers and import all data rows.
        String csv = String.join("\n",
                "Deal Unique Id,From Currency ISO Code,To Currency ISO Code,Deal Timestamp,Deal Amount in ordering currency",
                "D1,USD,EUR,2024-01-01T10:15:30+00:00,1200.55",
                "D2,GBP,JPY,2024-01-01T10:15:30+00:00,75.25");

        when(dealRepository.existsById("D1")).thenReturn(false);
        when(dealRepository.existsById("D2")).thenReturn(false);
        when(dealRepository.save(any(Deal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = new MockMultipartFile("file", "deals.csv", "text/csv", csv.getBytes());
        ImportResult result = dealImportService.importDeals(file);

        assertThat(result.totalRows()).isEqualTo(2);
        assertThat(result.importedRows()).isEqualTo(2);
        assertThat(result.duplicateRows()).isZero();
        assertThat(result.invalidRows()).isZero();
        assertThat(result.errors()).isEmpty();
        verify(dealRepository, times(2)).save(any(Deal.class));
    }

    @Test
    void shouldImportValidRowsAndSkipDuplicatesAndInvalidRows() {
        // Mixed outcomes in one batch validate that each counter is independently maintained.
        String csv = String.join("\n",
                "D1,USD,EUR,2024-01-01T10:15:30+00:00,1200.55",
                "D1,USD,EUR,2024-01-01T10:15:30+00:00,1200.55",
                "D2,US,EUR,2024-01-01T10:15:30+00:00,100",
                "D3,GBP,JPY,2024-01-01T10:15:30+00:00,75.25");

        when(dealRepository.existsById("D1")).thenReturn(false, true);
        when(dealRepository.existsById("D2")).thenReturn(false);
        when(dealRepository.existsById("D3")).thenReturn(false);
        when(dealRepository.save(any(Deal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = new MockMultipartFile("file", "deals.csv", "text/csv", csv.getBytes());
        ImportResult result = dealImportService.importDeals(file);

        assertThat(result.totalRows()).isEqualTo(4);
        assertThat(result.importedRows()).isEqualTo(2);
        assertThat(result.duplicateRows()).isEqualTo(1);
        assertThat(result.invalidRows()).isEqualTo(1);
        assertThat(result.errors()).hasSize(1);
        verify(dealRepository, times(2)).save(any(Deal.class));
    }

    @Test
    void shouldNormalizeCurrencyCodesAndTrimColumnsBeforeSaving() {
        // Parser normalization is important because CSVs often contain inconsistent casing/spaces.
        String csv = "D10, usd , jpy ,2024-01-01T10:15:30+00:00,10.50";

        when(dealRepository.existsById("D10")).thenReturn(false);
        when(dealRepository.save(any(Deal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = new MockMultipartFile("file", "deals.csv", "text/csv", csv.getBytes());
        ImportResult result = dealImportService.importDeals(file);

        ArgumentCaptor<Deal> savedDeal = ArgumentCaptor.forClass(Deal.class);
        verify(dealRepository).save(savedDeal.capture());

        assertThat(savedDeal.getValue().getFromCurrencyIsoCode()).isEqualTo("USD");
        assertThat(savedDeal.getValue().getToCurrencyIsoCode()).isEqualTo("JPY");
        assertThat(result.importedRows()).isEqualTo(1);
    }

    @Test
    void shouldTreatConstraintViolationDuringSaveAsDuplicate() {
        // existsById can race under concurrent imports, so save-time uniqueness must be tolerated.
        String csv = "D20,USD,EUR,2024-01-01T10:15:30+00:00,12.00";

        when(dealRepository.existsById("D20")).thenReturn(false);
        when(dealRepository.save(any(Deal.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        MockMultipartFile file = new MockMultipartFile("file", "deals.csv", "text/csv", csv.getBytes());
        ImportResult result = dealImportService.importDeals(file);

        assertThat(result.totalRows()).isEqualTo(1);
        assertThat(result.importedRows()).isZero();
        assertThat(result.duplicateRows()).isEqualTo(1);
        assertThat(result.invalidRows()).isZero();
    }

    @Test
    void shouldCollectParsingErrorsWithRowNumbers() {
        // Error aggregation with exact rows makes user remediation practical for large uploads.
        String csv = String.join("\n",
                "D1,USD,EUR,2024-01-01T10:15:30+00:00,1200.55",
                "D2,USD,EUR,not-a-date,10",
                "D3,USD,EUR,2024-01-01T10:15:30+00:00,not-a-number",
                "D4,USD,EUR,2024-01-01T10:15:30+00:00");

        when(dealRepository.existsById("D1")).thenReturn(false);
        when(dealRepository.save(any(Deal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = new MockMultipartFile("file", "deals.csv", "text/csv", csv.getBytes());
        ImportResult result = dealImportService.importDeals(file);

        assertThat(result.totalRows()).isEqualTo(4);
        assertThat(result.importedRows()).isEqualTo(1);
        assertThat(result.invalidRows()).isEqualTo(3);
        assertThat(result.errors())
                .extracting(error -> error.rowNumber() + ":" + error.message())
                .containsExactly(
                        "2:Invalid timestamp format. Expected ISO-8601 with offset",
                        "3:Invalid deal amount format",
                        "4:Row 4 must have exactly 5 columns"
                );
    }

    @Test
    void shouldRejectEmptyFile() {
        // Empty input should fail fast so callers know upload payload is fundamentally invalid.
        MockMultipartFile emptyFile = new MockMultipartFile("file", "deals.csv", "text/csv", new byte[0]);

        assertThatThrownBy(() -> dealImportService.importDeals(emptyFile))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("CSV file is required and cannot be empty");
    }
}
