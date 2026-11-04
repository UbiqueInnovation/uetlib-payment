package ch.uetlib.payment.datatrans.client.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

class DatatransEnumTest {

    private final ObjectMapper objectMapper = JsonMapper.shared();

    @Test
    void transactionType_serialization() {
        assertThat(objectMapper.writeValueAsString(DatatransTransactionType.PAYMENT))
                .isEqualTo("\"payment\"");
        assertThat(objectMapper.writeValueAsString(DatatransTransactionType.CREDIT))
                .isEqualTo("\"credit\"");
        assertThat(objectMapper.writeValueAsString(DatatransTransactionType.CARD_CHECK))
                .isEqualTo("\"card_check\"");

        assertThat(objectMapper.readValue("\"payment\"", DatatransTransactionType.class))
                .isEqualTo(DatatransTransactionType.PAYMENT);
        assertThat(objectMapper.readValue("\"credit\"", DatatransTransactionType.class))
                .isEqualTo(DatatransTransactionType.CREDIT);
        assertThat(objectMapper.readValue("\"card_check\"", DatatransTransactionType.class))
                .isEqualTo(DatatransTransactionType.CARD_CHECK);
    }

    @Test
    void transactionStatus_serialization() {
        assertThat(objectMapper.writeValueAsString(DatatransTransactionStatus.INITIALIZED))
                .isEqualTo("\"initialized\"");
        assertThat(objectMapper.writeValueAsString(DatatransTransactionStatus.SETTLED))
                .isEqualTo("\"settled\"");
        assertThat(objectMapper.writeValueAsString(DatatransTransactionStatus.FAILED))
                .isEqualTo("\"failed\"");

        assertThat(objectMapper.readValue("\"authorized\"", DatatransTransactionStatus.class))
                .isEqualTo(DatatransTransactionStatus.AUTHORIZED);
        assertThat(objectMapper.readValue("\"canceled\"", DatatransTransactionStatus.class))
                .isEqualTo(DatatransTransactionStatus.CANCELED);
    }

    @Test
    void failReason_serialization() {
        assertThat(
                        objectMapper.writeValueAsString(
                                DatatransTransactionStatusDetailFailReason.CARD_INVALID))
                .isEqualTo("\"card_invalid\"");
        assertThat(
                        objectMapper.writeValueAsString(
                                DatatransTransactionStatusDetailFailReason.DECLINED))
                .isEqualTo("\"declined\"");
        assertThat(
                        objectMapper.writeValueAsString(
                                DatatransTransactionStatusDetailFailReason.SOFT_DECLINED))
                .isEqualTo("\"soft_declined\"");

        assertThat(
                        objectMapper.readValue(
                                "\"timeout\"", DatatransTransactionStatusDetailFailReason.class))
                .isEqualTo(DatatransTransactionStatusDetailFailReason.TIMEOUT);
    }
}
