package ch.ubique.uetlib.payment.datatrans.internal;

import ch.ubique.uetlib.payment.PaymentFailureCode;
import ch.ubique.uetlib.payment.command.PaymentCancelCommand;
import ch.ubique.uetlib.payment.command.PaymentCaptureCommand;
import ch.ubique.uetlib.payment.result.PaymentOperationResult;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

public class DatatransOperationsClient {
    // TODO move settle/cancel requests to DatatransTransactionsFeignClient

    private final RestClient restClient;
    private final String datatransBaseUrl;
    private final String datatransMerchantId;
    private final String datatransPassword;

    public DatatransOperationsClient(
            final RestClient restClient,
            final String datatransBaseUrl,
            final String datatransMerchantId,
            final String datatransPassword) {
        this.restClient = restClient;
        this.datatransBaseUrl = datatransBaseUrl;
        this.datatransMerchantId = datatransMerchantId;
        this.datatransPassword = datatransPassword;
    }

    public PaymentOperationResult capture(final PaymentCaptureCommand command) {
        try {
            restClient
                    .post()
                    .uri(
                            datatransBaseUrl + "/v1/transactions/{transactionId}/settle",
                            command.providerTransactionId())
                    .header(HttpHeaders.AUTHORIZATION, basicAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(
                            Map.of(
                                    "refno", command.providerReference(),
                                    "amount",
                                            DatatransAmountConverter.toMinorUnits(command.amount()),
                                    "currency", command.currency().name()))
                    .retrieve()
                    .toBodilessEntity();
            return PaymentOperationResult.success();
        } catch (Exception e) {
            return PaymentOperationResult.failure(
                    PaymentFailureCode.PROVIDER_API_ERROR,
                    "Failed to capture Datatrans transaction");
        }
    }

    public PaymentOperationResult cancel(final PaymentCancelCommand command) {
        try {
            restClient
                    .post()
                    .uri(
                            datatransBaseUrl + "/v1/transactions/{transactionId}/cancel",
                            command.providerTransactionId())
                    .header(HttpHeaders.AUTHORIZATION, basicAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("refno", command.providerReference()))
                    .retrieve()
                    .toBodilessEntity();
            return PaymentOperationResult.success();
        } catch (Exception e) {
            return PaymentOperationResult.failure(
                    PaymentFailureCode.PROVIDER_API_ERROR,
                    "Failed to cancel Datatrans transaction");
        }
    }

    private String basicAuthHeader() {
        final var token = datatransMerchantId + ":" + datatransPassword;
        return "Basic "
                + Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }
}
