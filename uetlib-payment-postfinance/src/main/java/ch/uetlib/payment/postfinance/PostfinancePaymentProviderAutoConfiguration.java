package ch.uetlib.payment.postfinance;

import ch.uetlib.payment.postfinance.internal.PostfinanceHttpClientFactory;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import tools.jackson.databind.ObjectMapper;

@AutoConfiguration
@EnableConfigurationProperties(PostfinancePaymentProviderProperties.class)
public class PostfinancePaymentProviderAutoConfiguration {

    @Bean(name = "postfinanceHttpClient", destroyMethod = "close")
    public CloseableHttpClient postfinanceHttpClient(
            final PostfinancePaymentProviderProperties properties) {
        return PostfinanceHttpClientFactory.create(properties.httpLoggingEnabled());
    }

    @Bean
    public PostfinancePaymentProvider postfinancePaymentProvider(
            final ObjectMapper objectMapper,
            final PostfinancePaymentProviderProperties properties,
            @Qualifier("postfinanceHttpClient") final CloseableHttpClient httpClient) {
        return new PostfinancePaymentProvider(
                objectMapper,
                properties.spaceId(),
                properties.userId(),
                properties.authKey(),
                properties.productSku(),
                properties.productUniqueId(),
                properties.signatureValidationEnabled(),
                properties.environmentSelectionStrategy(),
                httpClient);
    }
}
