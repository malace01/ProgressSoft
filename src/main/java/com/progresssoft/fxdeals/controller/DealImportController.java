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

@RestController
@RequestMapping("/api/deals")
public class DealImportController {

    private final DealImportService dealImportService;

    public DealImportController(DealImportService dealImportService) {
        this.dealImportService = dealImportService;
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResult> importDeals(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(dealImportService.importDeals(file));
    }
}
