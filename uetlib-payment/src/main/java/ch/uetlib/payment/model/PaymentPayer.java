package ch.uetlib.payment.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.jspecify.annotations.Nullable;

/** Provider-neutral payer data passed to a payment provider. */
public record PaymentPayer(
        @NotBlank String customerReference,
        @Nullable String givenName,
        @Nullable String familyName,
        @Nullable @Email String email) {}
