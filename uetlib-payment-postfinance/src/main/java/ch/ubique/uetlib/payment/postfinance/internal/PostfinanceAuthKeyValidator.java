package ch.ubique.uetlib.payment.postfinance.internal;

import ch.ubique.uetlib.payment.postfinance.validation.PostFinanceAuthKey;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.jspecify.annotations.Nullable;

public class PostfinanceAuthKeyValidator
        implements ConstraintValidator<PostFinanceAuthKey, String> {

    private static final String ERROR_MESSAGE =
            "Expected a Base64-encoded key with at least 256 bits for HS256 signing.";

    public static void validate(final @Nullable String authKey) {
        if (isValidAuthKey(authKey)) {
            return;
        }

        throw new IllegalStateException("Invalid PostFinance auth key. " + ERROR_MESSAGE);
    }

    @Override
    public boolean isValid(
            final @Nullable String authKey, final ConstraintValidatorContext context) {
        if (isValidAuthKey(authKey)) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(ERROR_MESSAGE).addConstraintViolation();
        return false;
    }

    private static boolean isValidAuthKey(final @Nullable String authKey) {
        if (authKey == null) {
            return true;
        }

        try {
            final var decodedAuthKey =
                    Base64.getDecoder().decode(authKey.getBytes(StandardCharsets.UTF_8));
            Keys.hmacShaKeyFor(decodedAuthKey);
            return true;
        } catch (final IllegalArgumentException | WeakKeyException e) {
            return false;
        }
    }
}
