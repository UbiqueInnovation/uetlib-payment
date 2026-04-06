package ch.ubique.backend.payment.command;

/** Neutral command for canceling or voiding a provider-side payment transaction. */
public record PaymentCancelCommand(String providerTransactionId, String providerReference) {}
