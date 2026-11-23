package ch.uetlib.payment.datatrans.client.service.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import ch.uetlib.payment.datatrans.DatatransPaymentProvider;
import ch.uetlib.payment.datatrans.DatatransPaymentProviderAutoConfiguration;
import ch.uetlib.payment.datatrans.client.service.DatatransTransactionClient;
import ch.uetlib.payment.datatrans.client.service.feign.DatatransTransactionsFeignClient;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.json.JsonMapper;

class DatatransApiClientAutoConfigurationTest {
    private final ApplicationContextRunner runner =
            new ApplicationContextRunner()
                    .withConfiguration(
                            AutoConfigurations.of(
                                    FeignAutoConfiguration.class,
                                    DatatransApiClientAutoConfiguration.class,
                                    DatatransPaymentProviderAutoConfiguration.class));

    @Test
    void supportsStandaloneClientWithExistingProperties() {
        runner.withPropertyValues(
                        "spring.cloud.openfeign.client.config.datatrans-transactions.url=https://example.invalid",
                        "ch.uetlib.payment.datatrans.client.merchant-id=test-merchant",
                        "ch.uetlib.payment.datatrans.client.password=test-password")
                .run(context ->
                        assertThat(context)
                                .hasNotFailed()
                                .hasSingleBean(DatatransTransactionClient.class)
                                .hasSingleBean(DatatransTransactionsFeignClient.class)
                                .doesNotHaveBean(DatatransPaymentProvider.class));
    }

    @Test
    void registersClientOnceAlongsidePaymentProvider() {
        runner.withPropertyValues(
                        "ch.uetlib.payment.datatrans.base-url=https://example.invalid",
                        "ch.uetlib.payment.datatrans.webhook-url=https://example.invalid/webhook",
                        "ch.uetlib.payment.datatrans.merchant-id=test-merchant",
                        "ch.uetlib.payment.datatrans.password=test-password")
                .withBean(RestClient.class, RestClient::create)
                .withBean(
                        tools.jackson.databind.ObjectMapper.class,
                        () -> JsonMapper.builder().build())
                .run(context ->
                        assertThat(context)
                                .hasNotFailed()
                                .hasSingleBean(DatatransTransactionClient.class)
                                .hasSingleBean(DatatransTransactionsFeignClient.class)
                                .hasSingleBean(DatatransPaymentProvider.class));
    }
}
