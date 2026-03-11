package com.progresssoft.fxdeals.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.progresssoft.fxdeals.dto.ImportResult;
import com.progresssoft.fxdeals.exception.BadRequestException;
import com.progresssoft.fxdeals.service.DealImportService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Web-layer tests for {@link DealImportController}.
 *
 * <p>The goal is to validate HTTP contract shape independently from service internals.</p>
 */
@WebMvcTest(DealImportController.class)
class DealImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DealImportService dealImportService;

    @Test
    void shouldReturnImportSummary() throws Exception {
        // Successful uploads must expose all summary counters expected by API consumers.
        MockMultipartFile file = new MockMultipartFile("file", "deals.csv", "text/csv", "sample".getBytes());

        when(dealImportService.importDeals(file))
                .thenReturn(new ImportResult(10, 8, 1, 1, List.of()));

        mockMvc.perform(multipart("/api/deals/import").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRows").value(10))
                .andExpect(jsonPath("$.importedRows").value(8))
                .andExpect(jsonPath("$.duplicateRows").value(1))
                .andExpect(jsonPath("$.invalidRows").value(1));
    }

    @Test
    void shouldReturnBadRequestWhenServiceRejectsFile() throws Exception {
        // Validation/business failures must still follow the standardized error payload contract.
        MockMultipartFile file = new MockMultipartFile("file", "deals.csv", "text/csv", "sample".getBytes());

        when(dealImportService.importDeals(file))
                .thenThrow(new BadRequestException("CSV file is required and cannot be empty"));

        mockMvc.perform(multipart("/api/deals/import").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("CSV file is required and cannot be empty"))
                .andExpect(jsonPath("$.path").value("/api/deals/import"));
    }
}
