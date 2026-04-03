package ch.ubique.backend.payment.datatrans;

import ch.ubique.datatrans.service.DatatransTransactionClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@AutoConfiguration
public class DatatransPaymentProviderConfiguration {

    @Bean
    @ConditionalOnBean(DatatransTransactionClient.class)
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
}
