package ch.ubique.backend.payment.postfinance;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class PostfinancePaymentProviderConfiguration {

    @Bean
    public PostfinancePaymentProvider postfinancePaymentProvider(
            ObjectMapper objectMapper,
            @Value("${payment.postfinance.space-id}") long spaceId,
            @Value("${payment.postfinance.user-id}") long userId,
            @Value("${payment.postfinance.auth-key}") String authKey,
            @Value("${payment.postfinance.product.sku}") String productSku,
            @Value("${payment.postfinance.product.unique-id}") String productUniqueId,
            @Value("${payment.postfinance.webhook.signature-validation-enabled:true}")
                    boolean signatureValidationEnabled) {
        return new PostfinancePaymentProvider(
                objectMapper,
                spaceId,
                userId,
                authKey,
                productSku,
                productUniqueId,
                signatureValidationEnabled);
    }
}
