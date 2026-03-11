package com.progresssoft.fxdeals.dto;

import java.util.List;

public record ImportResult(int totalRows, int importedRows, int duplicateRows, int invalidRows, List<ImportError> errors) {
}
