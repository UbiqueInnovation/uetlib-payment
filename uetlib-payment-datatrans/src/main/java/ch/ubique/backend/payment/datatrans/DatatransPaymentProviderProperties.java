package ch.ubique.backend.payment.datatrans;

import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ch.ubique.backend.payment.datatrans")
public class DatatransPaymentProviderProperties {

    private final String webhookUrl;
    private final String baseUrl;
    private final String merchantId;
    private final String password;

    public DatatransPaymentProviderProperties(
            @NotBlank String webhookUrl,
            @NotBlank String baseUrl,
            @NotBlank String merchantId,
            @NotBlank String password) {
        this.webhookUrl = webhookUrl;
        this.baseUrl = baseUrl;
        this.merchantId = merchantId;
        this.password = password;
    }

    public String webhookUrl() {
        return webhookUrl;
    }

    public String baseUrl() {
        return baseUrl;
    }

    public String merchantId() {
        return merchantId;
    }

    public String password() {
        return password;
    }
}
