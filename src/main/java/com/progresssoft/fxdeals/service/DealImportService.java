package com.progresssoft.fxdeals.service;

import com.progresssoft.fxdeals.dto.DealRequest;
import com.progresssoft.fxdeals.dto.ImportError;
import com.progresssoft.fxdeals.dto.ImportResult;
import com.progresssoft.fxdeals.entity.Deal;
import com.progresssoft.fxdeals.exception.BadRequestException;
import com.progresssoft.fxdeals.repository.DealRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DealImportService {

    private static final Logger logger = LoggerFactory.getLogger(DealImportService.class);
    private static final int EXPECTED_COLUMNS = 5;

    private final DealRepository dealRepository;
    private final Validator validator;

    public DealImportService(DealRepository dealRepository, Validator validator) {
        this.dealRepository = dealRepository;
        this.validator = validator;
    }

    public ImportResult importDeals(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("CSV file is required and cannot be empty");
        }

        List<ImportError> errors = new ArrayList<>();
        int totalRows = 0;
        int importedRows = 0;
        int duplicateRows = 0;
        int invalidRows = 0;

        try (InputStream inputStream = file.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;
            int rowNumber = 0;
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                if (line.isBlank()) {
                    continue;
                }

                totalRows++;
                try {
                    DealRequest request = parseLine(line, rowNumber);
                    List<String> validationErrors = validate(request);
                    if (!validationErrors.isEmpty()) {
                        invalidRows++;
                        errors.add(new ImportError(rowNumber, String.join("; ", validationErrors)));
                        continue;
                    }

                    if (dealRepository.existsById(request.dealUniqueId())) {
                        duplicateRows++;
                        logger.info("Skipping duplicate deal id={} row={}", request.dealUniqueId(), rowNumber);
                        continue;
                    }

                    Deal deal = toEntity(request);
                    dealRepository.save(deal);
                    importedRows++;
                } catch (BadRequestException ex) {
                    invalidRows++;
                    errors.add(new ImportError(rowNumber, ex.getMessage()));
                } catch (DataIntegrityViolationException ex) {
                    duplicateRows++;
                    logger.info("Skipping deal because of constraint violation at row={} error={}", rowNumber, ex.getMessage());
                }
            }
        } catch (IOException ex) {
            throw new BadRequestException("Failed to read CSV file: " + ex.getMessage());
        }

        logger.info("Import completed: totalRows={}, importedRows={}, duplicateRows={}, invalidRows={}",
                totalRows, importedRows, duplicateRows, invalidRows);

        return new ImportResult(totalRows, importedRows, duplicateRows, invalidRows, errors);
    }

    private DealRequest parseLine(String line, int rowNumber) {
        String[] columns = line.split(",", -1);
        if (columns.length != EXPECTED_COLUMNS) {
            throw new BadRequestException("Row " + rowNumber + " must have exactly " + EXPECTED_COLUMNS + " columns");
        }

        try {
            return new DealRequest(
                    columns[0].trim(),
                    columns[1].trim().toUpperCase(),
                    columns[2].trim().toUpperCase(),
                    OffsetDateTime.parse(columns[3].trim()),
                    new BigDecimal(columns[4].trim())
            );
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Invalid timestamp format. Expected ISO-8601 with offset");
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Invalid deal amount format");
        }
    }

    private List<String> validate(DealRequest request) {
        Set<ConstraintViolation<DealRequest>> violations = validator.validate(request);
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    private Deal toEntity(DealRequest request) {
        Deal deal = new Deal();
        deal.setDealUniqueId(request.dealUniqueId());
        deal.setFromCurrencyIsoCode(request.fromCurrencyIsoCode());
        deal.setToCurrencyIsoCode(request.toCurrencyIsoCode());
        deal.setDealTimestamp(request.dealTimestamp());
        deal.setDealAmount(request.dealAmount());
        return deal;
    }
}
