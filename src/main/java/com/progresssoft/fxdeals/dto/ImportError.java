package com.progresssoft.fxdeals.dto;

/**
 * Row-level validation or parsing error collected during import.
 *
 * <p>Row number is preserved to make CSV correction workflows straightforward for users.</p>
 */
public record ImportError(int rowNumber, String message) {
}
