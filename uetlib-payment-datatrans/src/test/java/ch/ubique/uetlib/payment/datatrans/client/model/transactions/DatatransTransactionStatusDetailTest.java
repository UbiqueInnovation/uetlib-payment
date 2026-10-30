package ch.ubique.uetlib.payment.datatrans.client.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

class DatatransTransactionStatusDetailTest {

    private final ObjectMapper objectMapper = JsonMapper.shared();

    @Test
    void deserialize_authorizeDetail() {
        final var json =
                """
                {
                  "authorize": {
                    "amount": 150000,
                    "acquirerAuthorizationCode": "AUTH-123"
                  }
                }
                """;

        final var detail = objectMapper.readValue(json, DatatransTransactionStatusDetail.class);

        assertThat(detail.authorize()).isNotNull();
        assertThat(detail.authorize().amount()).isEqualTo(150000L);
        assertThat(detail.authorize().acquirerAuthorizationCode()).isEqualTo("AUTH-123");
        assertThat(detail.settle()).isNull();
    }

    @Test
    void deserialize_settleDetail() {
        final var json =
                """
                {
                  "settle": {
                    "amount": 999999L
                  }
                }
                """
                        .replace("L", "");

        final var detail = objectMapper.readValue(json, DatatransTransactionStatusDetail.class);

        assertThat(detail.settle()).isNotNull();
        assertThat(detail.settle().amount()).isEqualTo(999999L);
    }

    @Test
    void deserialize_creditDetail() {
        final var json =
                """
                {
                  "credit": {
                    "amount": 500
                  }
                }
                """;

        final var detail = objectMapper.readValue(json, DatatransTransactionStatusDetail.class);

        assertThat(detail.credit()).isNotNull();
        assertThat(detail.credit().amount()).isEqualTo(500L);
    }

    @Test
    void deserialize_cancelDetail() {
        final var json =
                """
                {
                  "cancel": {
                    "reversal": true
                  }
                }
                """;

        final var detail = objectMapper.readValue(json, DatatransTransactionStatusDetail.class);

        assertThat(detail.cancel()).isNotNull();
        assertThat(detail.cancel().reversal()).isTrue();
    }

    @Test
    void deserialize_initDetail() {
        final var json =
                """
                {
                  "init": {
                    "expires": "2026-07-23T12:00:00Z"
                  }
                }
                """;

        final var detail = objectMapper.readValue(json, DatatransTransactionStatusDetail.class);

        assertThat(detail.init()).isNotNull();
        assertThat(detail.init().expires()).isEqualTo(ZonedDateTime.parse("2026-07-23T12:00:00Z"));
    }

    @Test
    void deserialize_failDetail() {
        final var json =
                """
                {
                  "fail": {
                    "reason": "declined",
                    "message": "Card declined by issuer"
                  }
                }
                """;

        final var detail = objectMapper.readValue(json, DatatransTransactionStatusDetail.class);

        assertThat(detail.fail()).isNotNull();
        assertThat(detail.fail().reason())
                .isEqualTo(DatatransTransactionStatusDetailFailReason.DECLINED);
        assertThat(detail.fail().message()).isEqualTo("Card declined by issuer");
    }
}
