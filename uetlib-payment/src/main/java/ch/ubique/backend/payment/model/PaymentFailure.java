package ch.ubique.backend.payment.model;

import ch.ubique.backend.payment.PaymentFailureCode;

import org.jspecify.annotations.NonNull;

/** Neutral payment-provider error information. */
public record PaymentFailure(@NonNull PaymentFailureCode code, @NonNull String message) {}
