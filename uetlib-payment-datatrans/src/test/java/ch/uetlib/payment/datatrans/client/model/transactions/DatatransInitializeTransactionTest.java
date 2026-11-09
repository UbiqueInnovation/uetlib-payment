package ch.uetlib.payment.datatrans.client.model.transactions;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

class DatatransInitializeTransactionTest {

    private final ObjectMapper objectMapper = JsonMapper.shared();

    @Test
    void builder_createsValidObject() {
        final var request =
                DatatransInitializeTransaction.builder()
                        .currency(DatatransCurrency.CHF)
                        .refno("REF-12345")
                        .amount(1000L)
                        .paymentMethods(
                                List.of(DatatransPaymentMethod.VIS, DatatransPaymentMethod.ECA))
                        .autoSettle(true)
                        .language("de")
                        .redirect(
                                new DatatransInitializeTransactionRedirect(
                                        "https://success",
                                        "https://cancel",
                                        "https://error",
                                        "POST"))
                        .webhook(new DatatransInitializeTransactionWebhook("https://webhook"))
                        .build();

        assertThat(request.currency()).isEqualTo(DatatransCurrency.CHF);
        assertThat(request.refno()).isEqualTo("REF-12345");
        assertThat(request.amount()).isEqualTo(1000L);
        assertThat(request.paymentMethods())
                .containsExactly(DatatransPaymentMethod.VIS, DatatransPaymentMethod.ECA);
        assertThat(request.autoSettle()).isTrue();
        assertThat(request.language()).isEqualTo("de");
        assertThat(request.redirect()).isNotNull();
        assertThat(request.redirect().successUrl()).isEqualTo("https://success");
        assertThat(request.webhook()).isNotNull();
        assertThat(request.webhook().url()).isEqualTo("https://webhook");
    }

    @Test
    @SuppressWarnings("java:S5778")
    void builder_throwsOnMissingRequiredFields() {
        assertThatThrownBy(() -> DatatransInitializeTransaction.builder().refno("REF-1").build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("currency must not be null");

        assertThatThrownBy(
                        () ->
                                DatatransInitializeTransaction.builder()
                                        .currency(DatatransCurrency.CHF)
                                        .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("refno must not be null");
    }

    @Test
    void jackson_serializationAndDeserialization() throws Exception {
        final var request =
                DatatransInitializeTransaction.builder()
                        .currency(DatatransCurrency.CHF)
                        .refno("REF-999")
                        .amount(2500L)
                        .paymentMethods(List.of(DatatransPaymentMethod.TWI))
                        .build();

        final var json = objectMapper.writeValueAsString(request);
        assertThat(json)
                .contains("\"currency\":\"CHF\"")
                .contains("\"refno\":\"REF-999\"")
                .contains("\"amount\":2500")
                .contains("\"paymentMethods\":[\"TWI\"]");

        final var deserialized = objectMapper.readValue(json, DatatransInitializeTransaction.class);
        assertThat(deserialized.currency()).isEqualTo(DatatransCurrency.CHF);
        assertThat(deserialized.refno()).isEqualTo("REF-999");
        assertThat(deserialized.amount()).isEqualTo(2500L);
        assertThat(deserialized.paymentMethods()).containsExactly(DatatransPaymentMethod.TWI);
    }
}
