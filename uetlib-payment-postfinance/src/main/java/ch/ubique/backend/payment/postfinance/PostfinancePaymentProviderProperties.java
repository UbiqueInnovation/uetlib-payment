package ch.ubique.backend.payment.postfinance;

import ch.ubique.backend.payment.postfinance.validation.PostFinanceAuthKey;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "ch.ubique.backend.payment.postfinance")
public record PostfinancePaymentProviderProperties(
        long spaceId,
        long userId,
        @NotNull @PostFinanceAuthKey String authKey,
        @NotBlank String productSku,
        @NotBlank String productUniqueId,
        @DefaultValue("true") boolean signatureValidationEnabled,
        @DefaultValue("false") boolean httpLoggingEnabled) {}

