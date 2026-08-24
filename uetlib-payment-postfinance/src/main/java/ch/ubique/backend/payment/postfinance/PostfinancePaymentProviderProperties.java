package ch.ubique.backend.payment.postfinance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "ch.ubique.backend.payment.postfinance")
public record PostfinancePaymentProviderProperties(
        long spaceId,
        long userId,
        @NotBlank String authKey,
        @NotBlank String productSku,
        @NotBlank String productUniqueId,
        @DefaultValue("true") boolean signatureValidationEnabled,
        @DefaultValue("false") boolean httpLoggingEnabled,
        @NotNull
        @DefaultValue("USE_CONFIGURATION")
                PostfinanceEnvironmentSelection environmentSelectionStrategy) {

    @ConstructorBinding
    public PostfinancePaymentProviderProperties {}

    public PostfinancePaymentProviderProperties(
            long spaceId,
            long userId,
            String authKey,
            String productSku,
            String productUniqueId,
            boolean signatureValidationEnabled,
            boolean httpLoggingEnabled) {
        this(
                spaceId,
                userId,
                authKey,
                productSku,
                productUniqueId,
                signatureValidationEnabled,
                httpLoggingEnabled,
                PostfinanceEnvironmentSelection.USE_CONFIGURATION);
    }
}
