package ch.ubique.backend.payment.postfinance.internal;

import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class PostfinanceAuthKeyValidator {

    private PostfinanceAuthKeyValidator() {}

    public static void validate(final String authKey) {
        try {
            final byte[] decodedAuthKey =
                    Base64.getDecoder().decode(authKey.getBytes(StandardCharsets.UTF_8));
            Keys.hmacShaKeyFor(decodedAuthKey);
        } catch (IllegalArgumentException | WeakKeyException e) {
            throw new IllegalStateException(
                    "Invalid configuration for property ch.ubique.backend.payment.postfinance.auth-key. "
                            + "Expected a Base64-encoded key with at least 256 bits for HS256 signing.",
                    e);
        }
    }
}
