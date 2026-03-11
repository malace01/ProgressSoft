package com.progresssoft.fxdeals.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record DealRequest(
        @NotBlank(message = "Deal unique id is required")
        String dealUniqueId,

        @NotBlank(message = "From currency ISO code is required")
        @Pattern(regexp = "^[A-Z]{3}$", message = "From currency must be ISO-4217 3 uppercase letters")
        String fromCurrencyIsoCode,

        @NotBlank(message = "To currency ISO code is required")
        @Pattern(regexp = "^[A-Z]{3}$", message = "To currency must be ISO-4217 3 uppercase letters")
        String toCurrencyIsoCode,

        @NotNull(message = "Deal timestamp is required")
        OffsetDateTime dealTimestamp,

        @NotNull(message = "Deal amount is required")
        @DecimalMin(value = "0.0001", inclusive = true, message = "Deal amount must be greater than zero")
        BigDecimal dealAmount
) {
}
