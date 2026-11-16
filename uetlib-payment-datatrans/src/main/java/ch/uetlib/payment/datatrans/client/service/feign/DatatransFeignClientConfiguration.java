package ch.uetlib.payment.datatrans.client.service.feign;

import feign.auth.BasicAuthRequestInterceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class DatatransFeignClientConfiguration {

    @Bean
    public BasicAuthRequestInterceptor requestInterceptor(
            @Value("${ch.uetlib.payment.datatrans.client.merchant-id}")
                    final String datatransMerchantId,
            @Value("${ch.uetlib.payment.datatrans.client.password}")
                    final String datatransPassword) {
        return new BasicAuthRequestInterceptor(datatransMerchantId, datatransPassword);
    }
}
