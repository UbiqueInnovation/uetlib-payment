package ch.ubique.backend.payment.postfinance;

import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "ch.ubique.backend.payment.postfinance")
public class PostfinancePaymentProviderProperties {

    private final long spaceId;
    private final long userId;
    private final String authKey;
    private final String productSku;
    private final String productUniqueId;
    private final boolean signatureValidationEnabled;

    public PostfinancePaymentProviderProperties(
            long spaceId,
            long userId,
            @NotBlank String authKey,
            @NotBlank String productSku,
            @NotBlank String productUniqueId,
            @DefaultValue("true") boolean signatureValidationEnabled) {
        this.spaceId = spaceId;
        this.userId = userId;
        this.authKey = authKey;
        this.productSku = productSku;
        this.productUniqueId = productUniqueId;
        this.signatureValidationEnabled = signatureValidationEnabled;
    }

    public long spaceId() {
        return spaceId;
    }

    public long userId() {
        return userId;
    }

    public String authKey() {
        return authKey;
    }

    public String productSku() {
        return productSku;
    }

    public String productUniqueId() {
        return productUniqueId;
    }

    public boolean signatureValidationEnabled() {
        return signatureValidationEnabled;
    }
}
