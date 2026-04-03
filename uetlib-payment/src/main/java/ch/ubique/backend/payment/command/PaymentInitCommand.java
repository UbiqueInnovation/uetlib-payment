package ch.ubique.backend.payment.command;

import ch.ubique.backend.payment.PaymentCaptureMode;
import ch.ubique.backend.payment.PaymentCurrency;
import ch.ubique.backend.payment.model.PaymentBillingAddress;
import ch.ubique.backend.payment.model.PaymentPayer;

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
