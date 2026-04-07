package ch.ubique.backend.payment.command;

import ch.ubique.backend.payment.PaymentCaptureMode;
import ch.ubique.backend.payment.PaymentCurrency;
import ch.ubique.backend.payment.model.PaymentBillingAddress;
import ch.ubique.backend.payment.model.PaymentPayer;

import java.math.BigDecimal;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Provider-neutral command used to initiate a payment transaction. */
public record PaymentInitCommand(
        @NonNull BigDecimal amount,
        @NonNull PaymentCurrency currency,
        @NonNull String reference,
        @NonNull String description,
        @Nullable String successUrl,
        @Nullable String failedUrl,
        @NonNull PaymentCaptureMode captureMode,
        @Nullable PaymentPayer payer,
        @Nullable PaymentBillingAddress billingAddress) {}
