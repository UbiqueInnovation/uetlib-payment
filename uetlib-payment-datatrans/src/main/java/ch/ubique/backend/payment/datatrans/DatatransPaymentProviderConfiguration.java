package ch.ubique.backend.payment.datatrans;

import ch.ubique.datatrans.service.DatatransTransactionClient;
import ch.ubique.datatrans.service.feign.DatatransTransactionsFeignClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestClient;

@AutoConfiguration
@ConditionalOnClass(EnableFeignClients.class)
@PropertySource("classpath:/datatrans-feign.properties")
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class DatatransPaymentProviderConfiguration {

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
            @Value("${payment.datatrans.webhook-url}") final String paymentWebhookUrl,
            @Value("${payment.datatrans.base-url}") final String datatransBaseUrl,
            @Value("${payment.datatrans.merchant-id}") final String datatransMerchantId,
            @Value("${payment.datatrans.password}") final String datatransPassword) {
        return new DatatransPaymentProvider(
                datatransTransactionClient,
                restClient,
                objectMapper,
                paymentWebhookUrl,
                datatransBaseUrl,
                datatransMerchantId,
                datatransPassword);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableFeignClients(clients = DatatransTransactionsFeignClient.class)
    static class DatatransFeignClientConfiguration {}
}
