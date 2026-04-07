package ch.ubique.backend.payment.postfinance;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(PostfinancePaymentProviderProperties.class)
public class PostfinancePaymentProviderAutoConfiguration {

    @Bean
    public PostfinancePaymentProvider postfinancePaymentProvider(
            ObjectMapper objectMapper, final PostfinancePaymentProviderProperties properties) {
        return new PostfinancePaymentProvider(
                objectMapper,
                properties.spaceId(),
                properties.userId(),
                properties.authKey(),
                properties.productSku(),
                properties.productUniqueId(),
                properties.signatureValidationEnabled());
    }
}
