package com.progresssoft.fxdeals.controller;

import com.progresssoft.fxdeals.dto.ImportResult;
import com.progresssoft.fxdeals.service.DealImportService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Thin HTTP adapter around the import service.
 *
 * <p>The controller delegates all business decisions to the service so validation, duplicate
 * handling, and import accounting remain centralized in one place.</p>
 */
@RestController
@RequestMapping("/api/deals")
public class DealImportController {

    private final DealImportService dealImportService;

    public DealImportController(DealImportService dealImportService) {
        this.dealImportService = dealImportService;
    }

    /**
     * Accepts a multipart CSV file and returns an import summary.
     *
     * <p>Returning structured counters (instead of only a status code) helps clients reconcile
     * successful, duplicate, and invalid rows after each upload.</p>
     */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResult> importDeals(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(dealImportService.importDeals(file));
    }
}
