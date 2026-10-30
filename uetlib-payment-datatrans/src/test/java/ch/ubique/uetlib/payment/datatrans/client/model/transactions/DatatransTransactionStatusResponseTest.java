package ch.ubique.uetlib.payment.datatrans.client.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

class DatatransTransactionStatusResponseTest {

    private final ObjectMapper objectMapper = JsonMapper.shared();

    @Test
    void deserialize_cardTransactionStatusResponse() {
        final var json =
                """
                {
                  "transactionId": "240313170447077783",
                  "merchantId": "1100007283",
                  "type": "payment",
                  "status": "settled",
                  "currency": "CHF",
                  "refno": "order-123",
                  "refno2": "cust-456",
                  "paymentMethod": "VIS",
                  "detail": {
                    "settle": {
                      "amount": 1000
                    }
                  },
                  "card": {
                    "alias": "AAABcH0Bq92s3kgAESIAAbGj5NIsAHWC",
                    "fingerprint": "F-coGuJQ55tUk8CnKGI9PX1T",
                    "masked": "424242xxxxxx4242",
                    "expiryMonth": "12",
                    "expiryYear": "28"
                  }
                }
                """;

        final var response = objectMapper.readValue(json, DatatransTransactionStatusResponse.class);

        assertThat(response.transactionId()).isEqualTo("240313170447077783");
        assertThat(response.merchantId()).isEqualTo("1100007283");
        assertThat(response.type()).isEqualTo(DatatransTransactionType.PAYMENT);
        assertThat(response.status()).isEqualTo(DatatransTransactionStatus.SETTLED);
        assertThat(response.currency()).isEqualTo(DatatransCurrency.CHF);
        assertThat(response.refno()).isEqualTo("order-123");
        assertThat(response.refno2()).isEqualTo("cust-456");
        assertThat(response.paymentMethod()).isEqualTo(DatatransPaymentMethod.VIS);
        assertThat(response.detail().settle()).isNotNull();
        assertThat(response.detail().settle().amount()).isEqualTo(1000L);
        assertThat(response.card()).isNotNull();
        assertThat(response.card().alias()).isEqualTo("AAABcH0Bq92s3kgAESIAAbGj5NIsAHWC");
        assertThat(response.card().fingerprint()).isEqualTo("F-coGuJQ55tUk8CnKGI9PX1T");
        assertThat(response.card().masked()).isEqualTo("424242xxxxxx4242");
        assertThat(response.card().expiryMonth()).isEqualTo("12");
        assertThat(response.card().expiryYear()).isEqualTo("28");
    }

    @Test
    void deserialize_nonCardTransactionStatusResponseWithoutCardAndRefno2() {
        final var json =
                """
                {
                  "transactionId": "240313170447077784",
                  "merchantId": "1100007283",
                  "type": "payment",
                  "status": "authorized",
                  "currency": "CHF",
                  "refno": "order-789",
                  "paymentMethod": "TWI",
                  "detail": {
                    "authorize": {
                      "amount": 5000,
                      "acquirerAuthorizationCode": "AUTH-99"
                    }
                  }
                }
                """;

        final var response = objectMapper.readValue(json, DatatransTransactionStatusResponse.class);

        assertThat(response.transactionId()).isEqualTo("240313170447077784");
        assertThat(response.refno2()).isNull();
        assertThat(response.card()).isNull();
        assertThat(response.paymentMethod()).isEqualTo(DatatransPaymentMethod.TWI);
        assertThat(response.detail().authorize()).isNotNull();
        assertThat(response.detail().authorize().amount()).isEqualTo(5000L);
        assertThat(response.detail().authorize().acquirerAuthorizationCode()).isEqualTo("AUTH-99");
    }
}
