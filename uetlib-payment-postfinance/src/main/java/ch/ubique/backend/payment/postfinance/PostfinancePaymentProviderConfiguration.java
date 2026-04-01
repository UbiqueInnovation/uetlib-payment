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
            @Value("${postfinance.spaceId}") long spaceId,
            @Value("${postfinance.userId}") long userId,
            @Value("${postfinance.authKey}") String authKey,
            @Value("${postfinance.product.sku}") String productSku,
            @Value("${postfinance.product.uniqueId}") String productUniqueId,
            @Value("${postfinance.webhook.signatureValidationEnabled:true}")
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
