package ch.ubique.backend.payment.command;

import ch.ubique.backend.payment.PaymentCurrency;

import java.math.BigDecimal;

/** Neutral command for capturing or settling a previously authorized payment. */
public record PaymentCaptureCommand(
        String providerTransactionId,
        String providerReference,
        BigDecimal amount,
        PaymentCurrency currency) {}
