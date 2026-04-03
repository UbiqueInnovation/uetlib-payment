package ch.ubique.backend.payment;

import java.math.BigDecimal;

/** Provider-neutral command used to initiate a payment transaction. */
public record PaymentInitCommand(
        BigDecimal amount,
        PaymentCurrency currency,
        String reference,
        String description,
        String successUrl,
        String failedUrl,
        PaymentCaptureMode captureMode,
        PaymentPayer payer,
        PaymentBillingAddress billingAddress) {}
