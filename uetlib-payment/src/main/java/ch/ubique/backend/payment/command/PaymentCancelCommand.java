package ch.ubique.backend.payment.command;

import org.jspecify.annotations.NonNull;

/** Neutral command for canceling or voiding a provider-side payment transaction. */
public record PaymentCancelCommand(
        @NonNull String providerTransactionId, @NonNull String providerReference) {}
