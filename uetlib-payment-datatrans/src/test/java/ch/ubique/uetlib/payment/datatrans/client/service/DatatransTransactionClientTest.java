package ch.ubique.uetlib.payment.datatrans.client.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import ch.ubique.uetlib.payment.datatrans.client.model.transactions.*;
import ch.ubique.uetlib.payment.datatrans.client.service.feign.DatatransTransactionsFeignClient;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class DatatransTransactionClientTest {

    @Mock private DatatransTransactionsFeignClient feignClient;

    @InjectMocks private DatatransTransactionClient client;

    @Test
    void initializeTransaction_extractsLocationHeaderAndSetsRedirectUrl() {
        final var request =
                DatatransInitializeTransaction.builder()
                        .currency(DatatransCurrency.CHF)
                        .refno("REF-123")
                        .amount(1000L)
                        .build();

        final var mockBody =
                DatatransInitializeTransactionResponse.builder()
                        .transactionId("240313170447077783")
                        .redirectUrl("placeholder")
                        .build();

        final var headers = new HttpHeaders();
        headers.setLocation(
                URI.create("https://pay.sandbox.datatrans.com/v1/start/240313170447077783"));

        final var feignResponse = ResponseEntity.ok().headers(headers).body(mockBody);

        when(feignClient.initializeTransaction(request)).thenReturn(feignResponse);

        final var result = client.initializeTransaction(request);

        assertThat(result).isNotNull();
        assertThat(result.transactionId()).isEqualTo("240313170447077783");
        assertThat(result.redirectUrl())
                .isEqualTo("https://pay.sandbox.datatrans.com/v1/start/240313170447077783");

        verify(feignClient).initializeTransaction(request);
    }

    @Test
    void getTransactionStatus_delegatesToFeignClient() {
        final var trxId = "240313170447077783";
        final var expectedResponse = mock(DatatransTransactionStatusResponse.class);

        when(feignClient.getTransactionStatus(trxId)).thenReturn(expectedResponse);

        final var result = client.getTransactionStatus(trxId);

        assertThat(result).isSameAs(expectedResponse);
        verify(feignClient).getTransactionStatus(trxId);
    }
}
