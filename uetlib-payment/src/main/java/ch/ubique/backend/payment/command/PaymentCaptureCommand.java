package ch.ubique.backend.payment.command;

import ch.ubique.backend.payment.PaymentCurrency;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Neutral command for capturing or settling a previously authorized payment. */
public record PaymentCaptureCommand(
        @NotBlank String providerTransactionId,
        @NotBlank String providerReference,
        @NotNull @Positive BigDecimal amount,
        @NotNull PaymentCurrency currency) {}
