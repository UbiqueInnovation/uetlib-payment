package ch.ubique.backend.payment.datatrans.internal;

import ch.ubique.backend.payment.PaymentCaptureMode;
import ch.ubique.backend.payment.command.PaymentInitCommand;
import ch.ubique.datatrans.model.transactions.DatatransCurrency;
import ch.ubique.datatrans.model.transactions.DatatransInitializeTransaction;
import ch.ubique.datatrans.model.transactions.DatatransInitializeTransactionRedirect;
import ch.ubique.datatrans.model.transactions.DatatransInitializeTransactionTheme;
import ch.ubique.datatrans.model.transactions.DatatransInitializeTransactionWebhook;
import ch.ubique.datatrans.model.transactions.DatatransPaymentMethod;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Locale;

public class DatatransPaymentRequestFactory {

    private static final List<DatatransPaymentMethod> DEFAULT_PAYMENT_METHODS =
            List.of(
                    DatatransPaymentMethod.ECA,
                    DatatransPaymentMethod.VIS,
                    DatatransPaymentMethod.TWI,
                    DatatransPaymentMethod.APL,
                    DatatransPaymentMethod.PAY,
                    DatatransPaymentMethod.PFC,
                    DatatransPaymentMethod.PEF);

    private final String paymentWebhookUrl;

    public DatatransPaymentRequestFactory(final String paymentWebhookUrl) {
        this.paymentWebhookUrl = paymentWebhookUrl;
    }

    public DatatransInitializeTransaction createInitRequest(final PaymentInitCommand command) {
        return DatatransInitializeTransaction.builder()
                .currency(DatatransCurrency.CHF)
                .refno(command.reference())
                .amount(DatatransAmountConverter.toMinorUnits(command.amount()))
                .paymentMethods(DEFAULT_PAYMENT_METHODS)
                .autoSettle(command.captureMode() == PaymentCaptureMode.AUTOMATIC)
                .language(normalizeLanguage(LocaleContextHolder.getLocale()))
                .theme(new DatatransInitializeTransactionTheme())
                .redirect(
                        new DatatransInitializeTransactionRedirect(
                                command.successUrl(),
                                command.failedUrl(),
                                command.failedUrl(),
                                HttpMethod.GET.name()))
                .webhook(new DatatransInitializeTransactionWebhook(paymentWebhookUrl))
                .build();
    }

    private String normalizeLanguage(final Locale locale) {
        final var language = locale.getLanguage();
        if (language.length() != 2) {
            return "en";
        }
        return language;
    }
}
