package ch.ubique.backend.payment;

import java.math.BigDecimal;

/** Neutral command for capturing or settling a previously authorized payment. */
public record PaymentCaptureCommand(
        String providerTransactionId,
        String providerReference,
        BigDecimal amount,
        PaymentCurrency currency) {}
