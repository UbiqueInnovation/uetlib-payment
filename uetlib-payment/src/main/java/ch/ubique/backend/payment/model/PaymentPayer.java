package ch.ubique.backend.payment.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Provider-neutral payer data passed to a payment provider. */
public record PaymentPayer(
        @NotBlank String customerReference,
        String givenName,
        String familyName,
        @Email String email) {}
