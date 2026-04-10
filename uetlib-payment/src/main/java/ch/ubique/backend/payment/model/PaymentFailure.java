package ch.ubique.backend.payment.model;

import ch.ubique.backend.payment.PaymentFailureCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Neutral payment-provider error information. */
public record PaymentFailure(@NotNull PaymentFailureCode code, @NotBlank String message) {}
