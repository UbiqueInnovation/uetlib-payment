package ch.ubique.uetlib.payment.command;

import jakarta.validation.constraints.NotBlank;

/** Neutral command for canceling or voiding a provider-side payment transaction. */
public record PaymentCancelCommand(
        @NotBlank String providerTransactionId, @NotBlank String providerReference) {}
