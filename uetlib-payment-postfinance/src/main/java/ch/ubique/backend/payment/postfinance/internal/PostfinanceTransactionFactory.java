package ch.ubique.backend.payment.postfinance.internal;

import ch.postfinancecheckout.sdk.model.AddressCreate;
import ch.postfinancecheckout.sdk.model.LineItemCreate;
import ch.postfinancecheckout.sdk.model.LineItemType;
import ch.postfinancecheckout.sdk.model.TransactionCompletionBehavior;
import ch.postfinancecheckout.sdk.model.TransactionCreate;
import ch.ubique.backend.payment.PaymentCaptureMode;
import ch.ubique.backend.payment.command.PaymentInitCommand;
import ch.ubique.backend.payment.model.PaymentBillingAddress;
import ch.ubique.backend.payment.model.PaymentPayer;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

public class PostfinanceTransactionFactory {

    private final String productSku;
    private final String productUniqueId;

    public PostfinanceTransactionFactory(final String productSku, final String productUniqueId) {
        this.productSku = productSku;
        this.productUniqueId = productUniqueId;
    }

    public TransactionCreate create(final PaymentInitCommand command) {
        final var transaction = new TransactionCreate();
        final var payer = command.payer();
        transaction.setCurrency(command.currency().name());
        transaction.setMerchantReference(command.reference());
        transaction.setCompletionBehavior(resolveCompletionBehavior(command.captureMode()));
        if (command.successUrl() != null) {
            transaction.setSuccessUrl(command.successUrl());
        }
        if (command.failedUrl() != null) {
            transaction.setFailedUrl(command.failedUrl());
        }
        if (payer != null && StringUtils.isNotBlank(payer.email())) {
            transaction.setCustomerEmailAddress(payer.email());
        }
        final var billingAddress = buildBillingAddress(payer, command.billingAddress());
        if (billingAddress != null) {
            transaction.setBillingAddress(billingAddress);
        }
        transaction.setLineItems(java.util.List.of(createLineItem(command)));
        return transaction;
    }

    private TransactionCompletionBehavior resolveCompletionBehavior(
            final PaymentCaptureMode captureMode) {
        if (captureMode == PaymentCaptureMode.AUTOMATIC) {
            return TransactionCompletionBehavior.COMPLETE_IMMEDIATELY;
        }
        return TransactionCompletionBehavior.COMPLETE_DEFERRED;
    }

    private LineItemCreate createLineItem(final PaymentInitCommand command) {
        final var lineItem = new LineItemCreate();
        lineItem.setName(command.description());
        lineItem.setAmountIncludingTax(command.amount());
        lineItem.setQuantity(java.math.BigDecimal.ONE);
        lineItem.setType(LineItemType.PRODUCT);
        if (StringUtils.isNotBlank(productUniqueId)) {
            lineItem.setUniqueId(productUniqueId);
        }
        if (StringUtils.isNotBlank(productSku)) {
            lineItem.setSku(productSku);
        }
        return lineItem;
    }

    private @Nullable AddressCreate buildBillingAddress(
            final @Nullable PaymentPayer payer, final @Nullable PaymentBillingAddress billingAddressInput) {
        if (!hasPayerName(payer) && !hasBillingAddressData(billingAddressInput)) {
            return null;
        }
        final var billingAddress = new AddressCreate();
        applyPayerName(billingAddress, payer);
        applyBillingAddress(billingAddress, billingAddressInput);
        return billingAddress;
    }

    private boolean hasPayerName(final @Nullable PaymentPayer payer) {
        return payer != null
                && (StringUtils.isNotBlank(payer.givenName())
                        || StringUtils.isNotBlank(payer.familyName()));
    }

    private void applyPayerName(final AddressCreate billingAddress, final @Nullable PaymentPayer payer) {
        if (payer == null) {
            return;
        }
        setIfNotBlank(billingAddress::setGivenName, payer.givenName());
        setIfNotBlank(billingAddress::setFamilyName, payer.familyName());
    }

    private boolean hasBillingAddressData(final @Nullable PaymentBillingAddress address) {
        if (address == null) {
            return false;
        }
        return StringUtils.isNotBlank(address.street())
                || StringUtils.isNotBlank(address.houseNumber())
                || StringUtils.isNotBlank(address.addendum())
                || StringUtils.isNotBlank(address.city())
                || StringUtils.isNotBlank(address.zip())
                || StringUtils.isNotBlank(address.countryCode());
    }

    private void applyBillingAddress(
            final AddressCreate billingAddress, final @Nullable PaymentBillingAddress address) {
        if (address == null) {
            return;
        }
        setIfNotBlank(billingAddress::setStreet, formatStreet(address));
        setIfNotBlank(billingAddress::setPostcode, address.zip());
        setIfNotBlank(billingAddress::setCity, address.city());
        setIfNotBlank(billingAddress::setCountry, address.countryCode());
        setIfNotBlank(billingAddress::setDependentLocality, address.addendum());
    }

    private @Nullable String formatStreet(final PaymentBillingAddress address) {
        if (StringUtils.isNotBlank(address.street())
                && StringUtils.isNotBlank(address.houseNumber())) {
            return address.street() + " " + address.houseNumber();
        }
        if (StringUtils.isNotBlank(address.street())) {
            return address.street();
        }
        if (StringUtils.isNotBlank(address.houseNumber())) {
            return address.houseNumber();
        }
        return null;
    }

    private void setIfNotBlank(
            final java.util.function.Consumer<String> setter, final @Nullable String value) {
        if (StringUtils.isNotBlank(value)) {
            setter.accept(value);
        }
    }
}
