package ch.ubique.backend.payment.postfinance;

import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "ch.ubique.backend.payment.postfinance")
public record PostfinancePaymentProviderProperties(
        long spaceId,
        long userId,
        @NotBlank String authKey,
        @NotBlank String productSku,
        @NotBlank String productUniqueId,
        @DefaultValue("true") boolean signatureValidationEnabled) {}
