package com.progresssoft.fxdeals.exception;

/**
 * Signals client-correctable input issues (invalid/missing CSV content).
 *
 * <p>Keeping this separate from unexpected exceptions ensures API consumers receive
 * deterministic 400 responses for data quality problems.</p>
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
