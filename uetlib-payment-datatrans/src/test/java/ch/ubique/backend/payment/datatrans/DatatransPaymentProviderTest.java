package ch.ubique.backend.payment.datatrans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.ubique.backend.payment.PaymentCaptureMode;
import ch.ubique.backend.payment.PaymentCurrency;
import ch.ubique.backend.payment.PaymentMethod;
import ch.ubique.backend.payment.PaymentProviderType;
import ch.ubique.backend.payment.TransactionStatus;
import ch.ubique.backend.payment.command.PaymentCancelCommand;
import ch.ubique.backend.payment.command.PaymentCaptureCommand;
import ch.ubique.backend.payment.command.PaymentInitCommand;
import ch.ubique.backend.payment.model.PaymentPayer;
import ch.ubique.backend.payment.result.PaymentOperationResult;
import ch.ubique.datatrans.model.transactions.*;
import ch.ubique.datatrans.service.DatatransTransactionClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class DatatransPaymentProviderTest {

    @Mock private DatatransTransactionClient datatransTransactionClient;
    private RestClient restClient;
    private RestClient.RequestBodyUriSpec requestSpec;

    private DatatransPaymentProvider provider;

    @BeforeEach
    void setUp() {
        restClient = mock(RestClient.class);
        requestSpec =
                mock(RestClient.RequestBodyUriSpec.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        org.mockito.Mockito.lenient().when(restClient.post()).thenReturn(requestSpec);
        org.mockito.Mockito.lenient()
                .when(requestSpec.uri(anyString(), any(Object[].class)))
                .thenReturn(requestSpec);
        org.mockito.Mockito.lenient()
                .when(requestSpec.header(anyString(), any(String[].class)))
                .thenReturn(requestSpec);
        org.mockito.Mockito.lenient().when(requestSpec.contentType(any())).thenReturn(requestSpec);
        org.mockito.Mockito.lenient().when(requestSpec.body(any())).thenReturn(requestSpec);
        org.mockito.Mockito.lenient()
                .when(requestSpec.retrieve().toBodilessEntity())
                .thenReturn(ResponseEntity.ok().build());
        provider =
                new DatatransPaymentProvider(
                        datatransTransactionClient,
                        restClient,
                        new ObjectMapper(),
                        "https://example.com/webhook",
                        "https://api.sandbox.datatrans.com",
                        "merchant",
                        "password");
    }

    @Test
    void initiate_buildsDatatransTransactionAndReturnsNeutralResult() {
        when(datatransTransactionClient.initializeTransaction(any()))
                .thenReturn(
                        new DatatransInitializeTransactionResponse("trx-1", "https://redirect"));

        final var result =
                provider.initiate(
                        new PaymentInitCommand(
                                new BigDecimal("11.30"),
                                PaymentCurrency.CHF,
                                "ref-1",
                                "Workspace booking",
                                "https://example.com/success",
                                "https://example.com/cancel",
                                PaymentCaptureMode.MANUAL,
                                new PaymentPayer("public-user", null, null, null),
                                null));

        assertTrue(result.successful());
        assertEquals("trx-1", result.providerTransactionId());
        assertEquals("https://redirect", result.javascriptUrl());
        assertEquals(PaymentProviderType.DATATRANS, provider.getType());

        final ArgumentCaptor<DatatransInitializeTransaction> captor =
                ArgumentCaptor.forClass(DatatransInitializeTransaction.class);

        verify(datatransTransactionClient).initializeTransaction(captor.capture());
        assertEquals("ref-1", captor.getValue().refno());
        assertEquals(Integer.valueOf(1130), captor.getValue().amount());
        assertEquals(DatatransCurrency.CHF, captor.getValue().currency());
    }

    @Test
    void getStatus_mapsDatatransStatus() {
        when(datatransTransactionClient.getTransactionStatus("trx-1"))
                .thenReturn(
                        new DatatransTransactionStatusResponse(
                                "trx-1",
                                "merchant",
                                DatatransTransactionType.PAYMENT,
                                DatatransTransactionStatus.AUTHORIZED,
                                DatatransCurrency.CHF,
                                "ref-1",
                                null,
                                DatatransPaymentMethod.VIS,
                                null,
                                null));

        final var result = provider.getStatus("trx-1");

        assertTrue(result.successful());
        assertEquals(TransactionStatus.AUTHORIZED, result.paymentStatus());
        assertEquals("AUTHORIZED", result.rawProviderStatus());
        assertEquals("CHF", result.currency());
        assertEquals(PaymentMethod.CARD, result.paymentMethod());
    }

    @Test
    void processWebhook_parsesPayloadAndValidatesMerchant() {
        final var payload =
                new ObjectMapper()
                        .valueToTree(
                                new DatatransTransactionStatusResponse(
                                        "trx-1",
                                        "merchant",
                                        DatatransTransactionType.PAYMENT,
                                        DatatransTransactionStatus.SETTLED,
                                        DatatransCurrency.CHF,
                                        "ref-1",
                                        null,
                                        DatatransPaymentMethod.VIS,
                                        null,
                                        null))
                        .toString();
        final var result = provider.processWebhook(payload, Map.of());

        assertTrue(result.successful());
        assertEquals("trx-1", result.event().providerTransactionId());
        assertEquals(TransactionStatus.COMPLETED, result.event().paymentStatus());
    }

    @Test
    void processWebhook_rejectsForeignMerchant() {
        final var payload =
                new ObjectMapper()
                        .valueToTree(
                                new DatatransTransactionStatusResponse(
                                        "trx-1",
                                        "other",
                                        DatatransTransactionType.PAYMENT,
                                        DatatransTransactionStatus.SETTLED,
                                        DatatransCurrency.CHF,
                                        "ref-1",
                                        null,
                                        DatatransPaymentMethod.VIS,
                                        null,
                                        null))
                        .toString();
        final var result = provider.processWebhook(payload, Map.of());

        assertFalse(result.successful());
    }

    @Test
    void capture_callsDatatransSettleEndpoint() {
        final PaymentOperationResult result =
                provider.capture(
                        new PaymentCaptureCommand(
                                "trx-1", "ref-1", new BigDecimal("11.30"), PaymentCurrency.CHF));

        assertTrue(result.successful());
        verify(requestSpec)
                .uri(
                        "https://api.sandbox.datatrans.com/v1/transactions/{transactionId}/settle",
                        "trx-1");
        verify(requestSpec).body(Map.of("refno", "ref-1", "amount", 1130, "currency", "CHF"));
    }

    @Test
    void cancel_callsDatatransCancelEndpoint() {
        final PaymentOperationResult result =
                provider.cancel(new PaymentCancelCommand("trx-1", "ref-1"));

        assertTrue(result.successful());
        verify(requestSpec)
                .uri(
                        "https://api.sandbox.datatrans.com/v1/transactions/{transactionId}/cancel",
                        "trx-1");
        verify(requestSpec).body(Map.of("refno", "ref-1"));
    }
}
