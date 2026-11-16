package ch.uetlib.payment.datatrans.client.service.feign;

import ch.uetlib.payment.datatrans.client.model.transactions.DatatransInitializeTransaction;
import ch.uetlib.payment.datatrans.client.model.transactions.DatatransInitializeTransactionResponse;
import ch.uetlib.payment.datatrans.client.model.transactions.DatatransTransactionStatusResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Configure the url via spring property {@code
 * spring.cloud.openfeign.client.config.datatrans-transaction.url=URL}
 */
@FeignClient(
        name = "datatrans-transactions",
        path = "/v1/transactions",
        configuration = DatatransFeignClientConfiguration.class)
public interface DatatransTransactionsFeignClient {

    @PostMapping
    ResponseEntity<DatatransInitializeTransactionResponse> initializeTransaction(
            @RequestBody DatatransInitializeTransaction initializeTransaction);

    @GetMapping("/{transactionId}")
    DatatransTransactionStatusResponse getTransactionStatus(@PathVariable String transactionId);
}
