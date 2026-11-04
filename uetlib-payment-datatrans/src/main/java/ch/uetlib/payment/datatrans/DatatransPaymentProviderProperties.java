package ch.uetlib.payment.datatrans;

import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ch.ubique.backend.payment.datatrans")
public record DatatransPaymentProviderProperties(
        @NotBlank String webhookUrl,
        @NotBlank String baseUrl,
        @NotBlank String merchantId,
        @NotBlank String password) {}
