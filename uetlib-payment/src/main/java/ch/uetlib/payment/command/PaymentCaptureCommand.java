package ch.uetlib.payment.command;

import ch.uetlib.payment.PaymentCurrency;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** Neutral command for capturing or settling a previously authorized payment. */
public record PaymentCaptureCommand(
        @NotBlank String providerTransactionId,
        @NotBlank String providerReference,
        @NotNull @Positive BigDecimal amount,
        @NotNull PaymentCurrency currency) {}
