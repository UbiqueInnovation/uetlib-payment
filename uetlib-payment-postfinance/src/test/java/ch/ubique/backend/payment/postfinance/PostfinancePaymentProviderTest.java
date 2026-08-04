package ch.ubique.backend.payment.postfinance;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.postfinancecheckout.sdk.ApiException;
import ch.postfinancecheckout.sdk.model.Transaction;
import ch.postfinancecheckout.sdk.model.TransactionCompletion;
import ch.postfinancecheckout.sdk.model.TransactionCompletionState;
import ch.postfinancecheckout.sdk.model.TransactionCreate;
import ch.postfinancecheckout.sdk.model.TransactionState;
import ch.postfinancecheckout.sdk.model.TransactionVoid;
import ch.postfinancecheckout.sdk.model.TransactionVoidState;
import ch.postfinancecheckout.sdk.service.TransactionsService;
import ch.ubique.backend.payment.PaymentCaptureMode;
import ch.ubique.backend.payment.PaymentCurrency;
import ch.ubique.backend.payment.PaymentProviderType;
import ch.ubique.backend.payment.TransactionStatus;
import ch.ubique.backend.payment.command.PaymentCancelCommand;
import ch.ubique.backend.payment.command.PaymentCaptureCommand;
import ch.ubique.backend.payment.command.PaymentInitCommand;
import ch.ubique.backend.payment.model.PaymentBillingAddress;
import ch.ubique.backend.payment.model.PaymentPayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;

class PostfinancePaymentProviderTest {

    private static final String TEST_PRODUCT_SKU = "CHMOB-SUB";
    private static final String TEST_PRODUCT_UNIQUE_ID = "CHMOB-SUB";
    private static final ObjectMapper jsonMapper = JsonMapper.builder().build();

    @Mock private TransactionsService transactionsService;
    private PostfinancePaymentProvider postfinancePaymentProvider;
    private final long testSpaceId = 1L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        postfinancePaymentProvider =
                spy(
                        new PostfinancePaymentProvider(
                                new ObjectMapper(),
                                testSpaceId,
                                1L,
                                "a".repeat(64),
                                TEST_PRODUCT_SKU,
                                TEST_PRODUCT_UNIQUE_ID,
                                false));
        doReturn(transactionsService).when(postfinancePaymentProvider).createTransactionsService();
    }

    @Test
    void initiateTransaction_success() throws ApiException {
        final var billingAddress =
                new PaymentBillingAddress("Main Street", "10", null, "Bern", "3000", "CH");
        final var command =
                new PaymentInitCommand(
                        new BigDecimal("100.00"),
                        PaymentCurrency.CHF,
                        "REG-11111111",
                        "Test Item",
                        "https://example.com/success",
                        "https://example.com/failed",
                        PaymentCaptureMode.MANUAL,
                        new PaymentPayer("12345", "Ada", "Lovelace", "ada@example.com"),
                        billingAddress);

        final var created = mock(Transaction.class);
        when(created.getId()).thenReturn(1L);
        when(created.getState()).thenReturn(TransactionState.AUTHORIZED);
        when(created.getCurrency()).thenReturn("CHF");
        when(created.getAuthorizationAmount()).thenReturn(new BigDecimal("100.00"));
        when(created.getVersion()).thenReturn(1);

        final var transactionCaptor = ArgumentCaptor.forClass(TransactionCreate.class);
        when(transactionsService.postPaymentTransactions(
                        anyLong(), transactionCaptor.capture(), any()))
                .thenReturn(created);
        when(transactionsService.getPaymentTransactionsIdLightboxJavascriptUrl(
                        anyLong(), anyLong()))
                .thenReturn("http://js.url");

        final var response = postfinancePaymentProvider.initiate(command);

        assertTrue(response.successful());
        assertEquals("1", response.providerTransactionId());
        assertEquals("http://js.url", response.javascriptUrl());
        assertNull(response.paymentPageUrl());
        verify(transactionsService, never())
                .getPaymentTransactionsIdPaymentPageUrl(anyLong(), anyLong());
        assertEquals("REG-11111111", response.providerReference());
        assertEquals(TransactionStatus.AUTHORIZED, response.status());
        final var sent = transactionCaptor.getValue();
        assertEquals("REG-11111111", sent.getMerchantReference());
        assertEquals("ada@example.com", sent.getCustomerEmailAddress());
        assertEquals("Ada", sent.getBillingAddress().getGivenName());
        assertEquals("Lovelace", sent.getBillingAddress().getFamilyName());
        assertEquals("Main Street 10", sent.getBillingAddress().getStreet());
        assertEquals("3000", sent.getBillingAddress().getPostcode());
        assertEquals("Bern", sent.getBillingAddress().getCity());
        assertEquals("CH", sent.getBillingAddress().getCountry());
        assertEquals(TEST_PRODUCT_UNIQUE_ID, sent.getLineItems().getFirst().getUniqueId());
        assertEquals(TEST_PRODUCT_SKU, sent.getLineItems().getFirst().getSku());
    }

    @Test
    void getTransactionStatus_success() throws ApiException {
        final var transaction = mock(Transaction.class);
        when(transaction.getState()).thenReturn(TransactionState.AUTHORIZED);
        when(transaction.getAuthorizationAmount()).thenReturn(new BigDecimal("100.00"));
        when(transaction.getVersion()).thenReturn(2);
        when(transaction.getFailureReason()).thenReturn(null);

        when(transactionsService.getPaymentTransactionsId(1L, testSpaceId, null))
                .thenReturn(transaction);

        final var response = postfinancePaymentProvider.getStatus("1");

        assertTrue(response.successful());
        assertEquals(TransactionStatus.AUTHORIZED, response.paymentStatus());
    }

    @Test
    void getType_returnsPostfinance() {
        assertEquals(PaymentProviderType.POSTFINANCE, postfinancePaymentProvider.getType());
    }

    @Test
    void captureTransaction_success() throws ApiException {
        final var completion = mock(TransactionCompletion.class);
        when(completion.getState()).thenReturn(TransactionCompletionState.SUCCESSFUL);
        when(transactionsService.postPaymentTransactionsIdCompletePartiallyOnline(
                        anyLong(), anyLong(), any(), any()))
                .thenReturn(completion);

        final var result =
                postfinancePaymentProvider.capture(
                        new PaymentCaptureCommand(
                                "1", "capture-ref", new BigDecimal("100.00"), PaymentCurrency.CHF));

        assertTrue(result.successful());
    }

    @Test
    void cancelTransaction_success() throws ApiException {
        final var transactionVoid = mock(TransactionVoid.class);
        when(transactionVoid.getState()).thenReturn(TransactionVoidState.SUCCESSFUL);
        when(transactionsService.postPaymentTransactionsIdVoidOnline(anyLong(), anyLong(), any()))
                .thenReturn(transactionVoid);

        final var result =
                postfinancePaymentProvider.cancel(new PaymentCancelCommand("1", "cancel-ref"));

        assertTrue(result.successful());
    }

    @Test
    void constructor_rejectsWeakAuthKey() {
        final var exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                new PostfinancePaymentProvider(
                                        jsonMapper,
                                        testSpaceId,
                                        1L,
                                        "VE9ETw==",
                                        TEST_PRODUCT_SKU,
                                        TEST_PRODUCT_UNIQUE_ID,
                                        false));

        assertEquals(
                "Invalid configuration for property ch.ubique.backend.payment.postfinance.auth-key. Expected a Base64-encoded key with at least 256 bits for HS256 signing.",
                exception.getMessage());
    }

    @Test
    void constructor_rejectsMalformedBase64AuthKey() {
        final var exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                new PostfinancePaymentProvider(
                                        jsonMapper,
                                        testSpaceId,
                                        1L,
                                        "not-base64!!!",
                                        TEST_PRODUCT_SKU,
                                        TEST_PRODUCT_UNIQUE_ID,
                                        false));

        assertEquals(
                "Invalid configuration for property ch.ubique.backend.payment.postfinance.auth-key. Expected a Base64-encoded key with at least 256 bits for HS256 signing.",
                exception.getMessage());
    }
}
