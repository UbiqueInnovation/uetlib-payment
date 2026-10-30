package ch.ubique.uetlib.payment.command;

import ch.ubique.uetlib.payment.PaymentCaptureMode;
import ch.ubique.uetlib.payment.PaymentCurrency;
import ch.ubique.uetlib.payment.model.PaymentBillingAddress;
import ch.ubique.uetlib.payment.model.PaymentPayer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

/** Provider-neutral command used to initiate a payment transaction. */
public record PaymentInitCommand(
        @NotNull @Positive BigDecimal amount,
        @NotNull PaymentCurrency currency,
        @NotBlank String reference,
        @NotBlank String description,
        @Nullable String successUrl,
        @Nullable String failedUrl,
        @NotNull PaymentCaptureMode captureMode,
        @Valid @Nullable PaymentPayer payer,
        @Valid @Nullable PaymentBillingAddress billingAddress) {}
