package ch.uetlib.payment.datatrans.client.service.autoconfigure;

import ch.uetlib.payment.datatrans.client.service.DatatransTransactionClient;
import ch.uetlib.payment.datatrans.client.service.feign.DatatransTransactionsFeignClient;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

@AutoConfiguration
@PropertySource("classpath:datatrans-feign.properties")
@EnableFeignClients(clients = DatatransTransactionsFeignClient.class)
public class DatatransApiClientAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public DatatransTransactionClient datatransTransactionClient(
            DatatransTransactionsFeignClient feignClient) {
        return new DatatransTransactionClient(feignClient);
    }
}
