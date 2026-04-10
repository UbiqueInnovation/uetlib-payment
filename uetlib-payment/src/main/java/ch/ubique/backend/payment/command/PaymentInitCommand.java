package ch.ubique.backend.payment.command;

import ch.ubique.backend.payment.PaymentCaptureMode;
import ch.ubique.backend.payment.PaymentCurrency;
import ch.ubique.backend.payment.model.PaymentBillingAddress;
import ch.ubique.backend.payment.model.PaymentPayer;

import java.math.BigDecimal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Provider-neutral command used to initiate a payment transaction. */
public record PaymentInitCommand(
        @NotNull @Positive BigDecimal amount,
        @NotNull PaymentCurrency currency,
        @NotBlank String reference,
        @NotBlank String description,
        String successUrl,
        String failedUrl,
        @NotNull PaymentCaptureMode captureMode,
        @Valid PaymentPayer payer,
        @Valid PaymentBillingAddress billingAddress) {}
