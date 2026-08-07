package ch.ubique.backend.payment.postfinance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

class PostfinancePaymentProviderPropertiesTest {

    private static final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validate_validAuthKey_hasNoViolations() {
        final var properties =
                new PostfinancePaymentProviderProperties(
                        1L, 1L, "a".repeat(64), "sku", "uniqueId", true, false);

        final var violations = validator.validate(properties);

        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_invalidAuthKey_hasViolationOnAuthKey() {
        final var properties =
                new PostfinancePaymentProviderProperties(
                        1L, 1L, "VE9ETw==", "sku", "uniqueId", true, false);

        final var violations = validator.validate(properties);

        assertFalse(violations.isEmpty());
        final var violation =
                violations.stream()
                        .filter(v -> "authKey".equals(v.getPropertyPath().toString()))
                        .findFirst()
                        .orElseThrow();
        assertEquals(
                "Expected a Base64-encoded key with at least 256 bits for HS256 signing.",
                violation.getMessage());
    }
}

