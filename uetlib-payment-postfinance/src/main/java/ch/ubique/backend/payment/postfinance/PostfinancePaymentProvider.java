package ch.ubique.backend.payment.postfinance;

import ch.postfinancecheckout.sdk.ApiClient;
import ch.postfinancecheckout.sdk.ApiException;
import ch.postfinancecheckout.sdk.model.AddressCreate;
import ch.postfinancecheckout.sdk.model.LineItemCreate;
import ch.postfinancecheckout.sdk.model.LineItemType;
import ch.postfinancecheckout.sdk.model.Transaction;
import ch.postfinancecheckout.sdk.model.TransactionCompletionBehavior;
import ch.postfinancecheckout.sdk.model.TransactionCreate;
import ch.postfinancecheckout.sdk.model.TransactionState;
import ch.postfinancecheckout.sdk.service.TransactionsService;
import ch.postfinancecheckout.sdk.service.WebhookEncryptionKeysService;
import ch.ubique.backend.payment.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PostfinancePaymentProvider implements PaymentProvider {

    private static final Logger logger = LoggerFactory.getLogger(PostfinancePaymentProvider.class);
    // PostFinance Checkout SDK / API docs:
    // https://checkout.postfinance.ch/doc/api/web-service
    // https://checkout.postfinance.ch/doc/api/web-service#TransactionService-create
    private static final String UNKNOWN_TRANSACTION_STATE_MESSAGE =
            "PostFinance transaction is missing a state";

    private final long spaceId;
    private final long userId;
    private final String authKey;
    private final String productSku;
    private final String productUniqueId;
    private final ObjectMapper objectMapper;
    private final boolean signatureValidationEnabled;

    public PostfinancePaymentProvider(
            ObjectMapper objectMapper,
            long spaceId,
            long userId,
            String authKey,
            String productSku,
            String productUniqueId,
            boolean signatureValidationEnabled) {
        this.objectMapper = objectMapper;
        this.spaceId = spaceId;
        this.userId = userId;
        this.authKey = authKey;
        this.productSku = productSku;
        this.productUniqueId = productUniqueId;
        this.signatureValidationEnabled = signatureValidationEnabled;
    }

    /**
     * Factory hook kept public so consuming modules can override it in tests without living in this
     * package.
     */
    public TransactionsService createTransactionsService() {
        ApiClient apiClient = new ApiClient(userId, authKey);
        return new TransactionsService(apiClient);
    }

    /**
     * Factory hook kept public so consuming modules can override it in tests without living in this
     * package.
     */
    public WebhookEncryptionKeysService createWebhookEncryptionKeysService() {
        ApiClient apiClient = new ApiClient(userId, authKey);
        return new WebhookEncryptionKeysService(apiClient);
    }

    @Override
    public PaymentProviderType getType() {
        return PaymentProviderType.POSTFINANCE;
    }

    @Override
    public PaymentInitResult initiate(PaymentInitCommand command) {
        TransactionsService service = createTransactionsService();
        String merchantReference = command.reference();
        // The neutral payment command is translated to the PostFinance transaction shape here.
        TransactionCreate tc = new TransactionCreate();
        tc.setCurrency(command.currency().name());
        tc.setMerchantReference(merchantReference);
        tc.setCompletionBehavior(resolveCompletionBehavior(command));
        if (command.successUrl() != null) {
            tc.setSuccessUrl(command.successUrl());
        }
        if (command.failedUrl() != null) {
            tc.setFailedUrl(command.failedUrl());
        }
        if (command.payer() != null && isNotBlank(command.payer().email())) {
            tc.setCustomerEmailAddress(command.payer().email());
        }
        AddressCreate billingAddress = buildBillingAddress(command);
        if (billingAddress != null) {
            tc.setBillingAddress(billingAddress);
        }

        LineItemCreate lineItem = new LineItemCreate();
        lineItem.setName(command.description());
        lineItem.setAmountIncludingTax(command.amount());
        lineItem.setQuantity(java.math.BigDecimal.ONE);
        lineItem.setType(LineItemType.PRODUCT);
        if (isNotBlank(productUniqueId)) {
            lineItem.setUniqueId(productUniqueId);
        }
        if (isNotBlank(productSku)) {
            lineItem.setSku(productSku);
        }
        tc.lineItems(java.util.List.of(lineItem));

        try {
            Transaction created = service.postPaymentTransactions(spaceId, tc, null);
            Long transactionId = created.getId();
            TransactionStatus status = getRequiredTransactionStatus(created);
            if (transactionId == null || status == null) {
                logger.error(
                        "PostFinance returned an incomplete transaction for reference {}",
                        merchantReference);
                return PaymentInitResult.failure(
                        PaymentFailureCode.INVALID_PROVIDER_RESPONSE,
                        "PostFinance returned an incomplete transaction");
            }

            // PostFinance Checkout opens through the Lightbox JavaScript URL returned for
            // the created transaction.
            String jsUrl =
                    service.getPaymentTransactionsIdLightboxJavascriptUrl(transactionId, spaceId);

            return PaymentInitResult.success(
                    transactionId.toString(),
                    jsUrl,
                    merchantReference,
                    status,
                    created.getCurrency(),
                    created.getAuthorizationAmount(),
                    created.getVersion());
        } catch (ApiException e) {
            logger.error(
                    "Failed to initiate transaction for customerReference {}",
                    command.payer() != null ? command.payer().customerReference() : null,
                    e);
            return PaymentInitResult.failure(
                    PaymentFailureCode.PROVIDER_API_ERROR,
                    "Failed to initiate PostFinance transaction");
        }
    }

    @Override
    public PaymentStatusResult getStatus(String providerTransactionId) {
        TransactionsService service = createTransactionsService();

        try {
            // Retrieve transaction
            Long postfinanceId = Long.valueOf(providerTransactionId);
            Transaction transaction =
                    service.getPaymentTransactionsId(postfinanceId, spaceId, null);

            TransactionStatus status = getRequiredTransactionStatus(transaction);
            if (status == null) {
                logger.error(
                        "PostFinance returned a transaction without state for providerTransactionId {}",
                        providerTransactionId);
                return PaymentStatusResult.failure(
                        PaymentFailureCode.INVALID_PROVIDER_RESPONSE,
                        "PostFinance returned a transaction without state");
            }
            String currency = transaction.getCurrency();
            String failureReason = extractFailureReason(transaction);
            return PaymentStatusResult.success(
                    status,
                    transaction.getState() == null ? null : transaction.getState().name(),
                    currency,
                    transaction.getAuthorizationAmount(),
                    PaymentMethod.UNKNOWN,
                    failureReason,
                    failureReason);
        } catch (ApiException e) {
            logger.error(
                    "Failed to get transaction status for providerTransactionId {}",
                    providerTransactionId,
                    e);
            return PaymentStatusResult.failure(
                    PaymentFailureCode.PROVIDER_API_ERROR,
                    "Failed to fetch PostFinance transaction status");
        } catch (NumberFormatException e) {
            logger.error(
                    "Invalid provider transaction id for PostFinance: {}",
                    providerTransactionId,
                    e);
            return PaymentStatusResult.failure(
                    PaymentFailureCode.INVALID_PROVIDER_REFERENCE,
                    "Invalid PostFinance transaction id");
        }
    }

    private TransactionCompletionBehavior resolveCompletionBehavior(PaymentInitCommand command) {
        if (command.captureMode() == PaymentCaptureMode.AUTOMATIC) {
            return TransactionCompletionBehavior.COMPLETE_IMMEDIATELY;
        }
        return TransactionCompletionBehavior.COMPLETE_DEFERRED;
    }

    @Override
    public PaymentWebhookResult processWebhook(String rawPayload, Map<String, String> headers) {
        String signatureHeader = headers != null ? headers.get("x-signature") : null;
        if (signatureValidationEnabled && !isSignatureValid(rawPayload, signatureHeader)) {
            logger.info("Rejected PostFinance webhook due to invalid or missing signature");
            return PaymentWebhookResult.failure(
                    PaymentFailureCode.INVALID_PROVIDER_REFERENCE,
                    "Invalid or missing PostFinance webhook signature");
        }

        PostfinanceWebhookPayload webhookPayload;
        try {
            webhookPayload = objectMapper.readValue(rawPayload, PostfinanceWebhookPayload.class);
        } catch (Exception e) {
            logger.info("Rejected PostFinance webhook due to invalid payload");
            logger.debug("Invalid PostFinance webhook payload", e);
            return PaymentWebhookResult.failure(
                    PaymentFailureCode.INVALID_PROVIDER_RESPONSE,
                    "Invalid PostFinance webhook payload");
        }

        if (!isSupportedTransactionWebhook(webhookPayload)) {
            return PaymentWebhookResult.ignoredResult();
        }

        TransactionStatus transactionStatus = resolveWebhookTransactionStatus(webhookPayload);
        if (transactionStatus == null) {
            logger.info(
                    "Unable to resolve PostFinance transaction state for entityId {}",
                    webhookPayload.entityId());
            return PaymentWebhookResult.failure(
                    PaymentFailureCode.INVALID_PROVIDER_RESPONSE,
                    "Unable to resolve PostFinance transaction state");
        }

        return PaymentWebhookResult.success(
                new PaymentWebhookEvent(
                        webhookPayload.entityId().toString(),
                        transactionStatus,
                        webhookPayload.state(),
                        null,
                        null,
                        null,
                        null));
    }

    private String extractFailureReason(Transaction transaction) {
        if (transaction.getFailureReason() == null) {
            return null;
        }
        String description =
                getPreferredLocalizedValue(transaction.getFailureReason().getDescription());
        if (isNotBlank(description)) {
            return description;
        }
        return getPreferredLocalizedValue(transaction.getFailureReason().getName());
    }

    private String getPreferredLocalizedValue(Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        String english = values.get("en-US");
        if (isNotBlank(english)) {
            return english;
        }
        return values.values().stream().filter(this::isNotBlank).findFirst().orElse(null);
    }

    private AddressCreate buildBillingAddress(PaymentInitCommand command) {
        var payer = command.payer();
        PaymentBillingAddress addr = command.billingAddress();
        if (!hasPayerName(payer) && !hasBillingAddressData(addr)) {
            return null;
        }
        AddressCreate billingAddress = new AddressCreate();
        applyPayerName(billingAddress, payer);
        applyBillingAddress(billingAddress, addr);
        return billingAddress;
    }

    private boolean hasBillingAddressData(PaymentBillingAddress addr) {
        if (addr == null) {
            return false;
        }
        return isNotBlank(addr.street())
                || isNotBlank(addr.houseNumber())
                || isNotBlank(addr.addendum())
                || isNotBlank(addr.city())
                || isNotBlank(addr.zip())
                || isNotBlank(addr.countryCode());
    }

    private String formatStreet(PaymentBillingAddress addr) {
        String street = addr.street();
        String houseNumber = addr.houseNumber();
        if (isNotBlank(street) && isNotBlank(houseNumber)) {
            return street + " " + houseNumber;
        }
        if (isNotBlank(street)) {
            return street;
        }
        if (isNotBlank(houseNumber)) {
            return houseNumber;
        }
        return null;
    }

    private TransactionStatus getRequiredTransactionStatus(Transaction transaction) {
        if (transaction == null || transaction.getState() == null) {
            logger.error(UNKNOWN_TRANSACTION_STATE_MESSAGE);
            return null;
        }
        return mapTransactionState(transaction.getState());
    }

    private boolean isSignatureValid(String payload, String signatureHeader) {
        if (signatureHeader == null || signatureHeader.isBlank()) {
            return false;
        }
        try {
            return createWebhookEncryptionKeysService().isContentValid(signatureHeader, payload);
        } catch (ApiException e) {
            logger.info("PostFinance webhook signature validation failed");
            logger.debug("PostFinance webhook signature validation exception", e);
            return false;
        }
    }

    private boolean isSupportedTransactionWebhook(PostfinanceWebhookPayload payload) {
        if (payload.entityId() == null) {
            logger.info("Ignoring PostFinance webhook without entityId");
            return false;
        }

        if (payload.spaceId() != null && payload.spaceId() != spaceId) {
            logger.info("Ignoring PostFinance webhook for foreign spaceId {}", payload.spaceId());
            return false;
        }

        String technicalName = payload.listenerEntityTechnicalName();
        if (technicalName != null && !"Transaction".equalsIgnoreCase(technicalName)) {
            logger.info(
                    "Ignoring PostFinance webhook for listenerEntityTechnicalName {}",
                    technicalName);
            return false;
        }
        return true;
    }

    private TransactionStatus resolveWebhookTransactionStatus(
            PostfinanceWebhookPayload webhookPayload) {
        if (webhookPayload.state() != null && !webhookPayload.state().isBlank()) {
            try {
                return mapTransactionState(TransactionState.valueOf(webhookPayload.state()));
            } catch (IllegalArgumentException e) {
                logger.info("Unknown PostFinance transaction state {}", webhookPayload.state());
            }
        }

        PaymentStatusResult statusResult = getStatus(webhookPayload.entityId().toString());
        if (!statusResult.successful()) {
            return null;
        }
        return statusResult.paymentStatus();
    }

    private TransactionStatus mapTransactionState(final TransactionState state) {
        if (state == null) {
            return null;
        }
        return switch (state) {
            case CREATE, PENDING, CONFIRMED, PROCESSING -> TransactionStatus.PENDING;
            case AUTHORIZED -> TransactionStatus.AUTHORIZED;
            case COMPLETED, FULFILL -> TransactionStatus.COMPLETED;
            case VOIDED -> TransactionStatus.VOIDED;
            case FAILED, DECLINE -> TransactionStatus.FAILED;
        };
    }

    private boolean hasPayerName(PaymentPayer payer) {
        return payer != null && (isNotBlank(payer.givenName()) || isNotBlank(payer.familyName()));
    }

    private void applyPayerName(AddressCreate billingAddress, PaymentPayer payer) {
        if (payer == null) {
            return;
        }
        if (isNotBlank(payer.givenName())) {
            billingAddress.setGivenName(payer.givenName());
        }
        if (isNotBlank(payer.familyName())) {
            billingAddress.setFamilyName(payer.familyName());
        }
    }

    private void applyBillingAddress(AddressCreate billingAddress, PaymentBillingAddress addr) {
        if (addr == null) {
            return;
        }
        setIfNotBlank(billingAddress::setStreet, formatStreet(addr));
        setIfNotBlank(billingAddress::setPostcode, addr.zip());
        setIfNotBlank(billingAddress::setCity, addr.city());
        setIfNotBlank(billingAddress::setCountry, addr.countryCode());
        setIfNotBlank(billingAddress::setDependentLocality, addr.addendum());
    }

    private void setIfNotBlank(java.util.function.Consumer<String> setter, String value) {
        if (isNotBlank(value)) {
            setter.accept(value);
        }
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
