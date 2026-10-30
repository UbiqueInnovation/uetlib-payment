package ch.ubique.uetlib.payment.datatrans.client.service;

import ch.ubique.uetlib.payment.datatrans.client.model.transactions.DatatransInitializeTransaction;
import ch.ubique.uetlib.payment.datatrans.client.model.transactions.DatatransInitializeTransactionResponse;
import ch.ubique.uetlib.payment.datatrans.client.model.transactions.DatatransTransactionStatusResponse;
import ch.ubique.uetlib.payment.datatrans.client.service.feign.DatatransTransactionsFeignClient;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class DatatransTransactionClient {

    private final DatatransTransactionsFeignClient transactionsFeignClient;

    public DatatransTransactionClient(
            final DatatransTransactionsFeignClient transactionsFeignClient) {
        this.transactionsFeignClient = transactionsFeignClient;
    }

    public DatatransInitializeTransactionResponse initializeTransaction(
            final DatatransInitializeTransaction initializeTransaction) {
        final var initializeTransactionResponse =
                transactionsFeignClient.initializeTransaction(initializeTransaction);
        final var redirectUrl =
                Objects.requireNonNull(initializeTransactionResponse.getHeaders().getLocation())
                        .toString();
        return Objects.requireNonNull(initializeTransactionResponse.getBody()).toBuilder()
                .redirectUrl(redirectUrl)
                .build();
    }

    public DatatransTransactionStatusResponse getTransactionStatus(final String transactionId) {
        return transactionsFeignClient.getTransactionStatus(transactionId);
    }
}
