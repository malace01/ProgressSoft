package com.progresssoft.fxdeals.exception;

import java.time.Instant;

/**
 * Uniform API error contract for both expected and unexpected failures.
 */
public record ApiErrorResponse(Instant timestamp, int status, String error, String message, String path) {
}
