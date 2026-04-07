package ch.ubique.backend.payment.datatrans;

import ch.ubique.datatrans.service.DatatransTransactionClient;
import ch.ubique.datatrans.service.feign.DatatransTransactionsFeignClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@AutoConfiguration
@ConditionalOnClass(EnableFeignClients.class)
@EnableConfigurationProperties(DatatransPaymentProviderProperties.class)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class DatatransPaymentProviderAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DatatransTransactionClient.class)
    public DatatransTransactionClient datatransTransactionClient(
            final DatatransTransactionsFeignClient datatransTransactionsFeignClient) {
        return new DatatransTransactionClient(datatransTransactionsFeignClient);
    }

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

    @Configuration(proxyBeanMethods = false)
    @EnableFeignClients(clients = DatatransTransactionsFeignClient.class)
    static class DatatransFeignClientConfiguration {}
}
