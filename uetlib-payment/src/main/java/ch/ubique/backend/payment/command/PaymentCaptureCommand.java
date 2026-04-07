package ch.ubique.backend.payment.command;

import ch.ubique.backend.payment.PaymentCurrency;

import java.math.BigDecimal;

import org.jspecify.annotations.NonNull;

/** Neutral command for capturing or settling a previously authorized payment. */
public record PaymentCaptureCommand(
        @NonNull String providerTransactionId,
        @NonNull String providerReference,
        @NonNull BigDecimal amount,
        @NonNull PaymentCurrency currency) {}
