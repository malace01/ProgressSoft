package com.progresssoft.fxdeals.dto;

import java.util.List;

/**
 * Aggregate outcome returned to API clients after processing an uploaded CSV file.
 *
 * <p>Counters are kept explicit so callers can distinguish duplicate rows from invalid rows and
 * decide whether to retry, fix data, or continue.</p>
 */
public record ImportResult(int totalRows, int importedRows, int duplicateRows, int invalidRows, List<ImportError> errors) {
}
