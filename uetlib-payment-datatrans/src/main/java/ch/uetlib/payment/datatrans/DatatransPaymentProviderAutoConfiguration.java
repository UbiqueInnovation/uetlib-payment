package ch.uetlib.payment.datatrans;

import ch.uetlib.payment.datatrans.client.service.DatatransTransactionClient;
import ch.uetlib.payment.datatrans.client.service.autoconfigure.DatatransApiClientAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.ObjectMapper;

@AutoConfiguration(after = DatatransApiClientAutoConfiguration.class)
@ConditionalOnProperty(prefix = "ch.ubique.backend.payment.datatrans", name = "base-url")
@ConditionalOnClass(EnableFeignClients.class)
@EnableConfigurationProperties(DatatransPaymentProviderProperties.class)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class DatatransPaymentProviderAutoConfiguration {

    @Bean
    public DatatransPaymentProvider datatransPaymentProvider(
            final DatatransTransactionClient datatransTransactionClient,
            final RestClient restClient,
            final ObjectMapper objectMapper,
            final DatatransPaymentProviderProperties properties) {
        return new DatatransPaymentProvider(
                datatransTransactionClient,
                restClient,
                objectMapper,
                properties.webhookUrl(),
                properties.baseUrl(),
                properties.merchantId(),
                properties.password());
    }
}
