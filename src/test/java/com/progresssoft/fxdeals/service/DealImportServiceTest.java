package com.progresssoft.fxdeals.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.progresssoft.fxdeals.dto.ImportResult;
import com.progresssoft.fxdeals.entity.Deal;
import com.progresssoft.fxdeals.repository.DealRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

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
}
