package ch.ubique.backend.payment.model;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Provider-neutral payer data passed to a payment provider. */
public record PaymentPayer(
        @NonNull String customerReference,
        @Nullable String givenName,
        @Nullable String familyName,
        @Nullable String email) {}
