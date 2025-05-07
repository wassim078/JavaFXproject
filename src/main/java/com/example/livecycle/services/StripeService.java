package com.example.livecycle.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Token;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodCreateParams;
import com.stripe.param.TokenCreateParams;
import java.util.regex.Pattern;

public class StripeService {
    private static final Pattern CARD_REGEX = Pattern.compile("^\\d{13,19}$");
    private static final Pattern CVC_REGEX = Pattern.compile("^\\d{3,4}$");

    static {




        String secretKey = System.getenv("STRIPE_SECRET_KEY");
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("STRIPE_SECRET_KEY environment variable not configured");
        }
        Stripe.apiKey = secretKey;
    }

    public Token createCardToken(String number, int expMonth, int expYear, String cvc) throws StripeException {
        validateCardDetails(number, expMonth, expYear, cvc);

        return Token.create(TokenCreateParams.builder()
                .setCard(TokenCreateParams.Card.builder()
                        .setNumber(cleanCardNumber(number))
                        .setExpMonth(String.valueOf(expMonth))
                        .setExpYear(String.valueOf(expYear))
                        .setCvc(cvc)
                        .build())
                .build());
    }

    public PaymentIntent createPaymentIntent(long amountCents, String currency, String paymentMethodId)
            throws StripeException {
        PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                .setAmount(amountCents)
                .setCurrency(currency.toLowerCase())
                .setConfirm(true);

        if (paymentMethodId != null) {
            paramsBuilder.setPaymentMethod(paymentMethodId)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                    .build()
                    );
        } else {
            paramsBuilder.setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                            .setEnabled(true)
                            .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                            .build()
            );
        }

        return PaymentIntent.create(paramsBuilder.build());
    }

    private void validateCardDetails(String number, int month, int year, String cvc) {
        if (!CARD_REGEX.matcher(cleanCardNumber(number)).matches()) {
            throw new IllegalArgumentException("Invalid card number");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Invalid expiration month");
        }
        if (year < 2023 || year > 2100) {
            throw new IllegalArgumentException("Invalid expiration year");
        }
        if (!CVC_REGEX.matcher(cvc).matches()) {
            throw new IllegalArgumentException("Invalid CVC");
        }
    }

    private String cleanCardNumber(String number) {
        return number.replaceAll("[^0-9]", "");
    }

    public PaymentMethod createPaymentMethod(String number, int expMonth, int expYear, String cvc)
            throws StripeException {
        validateCardDetails(number, expMonth, expYear, cvc);

        return PaymentMethod.create(PaymentMethodCreateParams.builder()
                .setType(PaymentMethodCreateParams.Type.CARD)
                .setCard(PaymentMethodCreateParams.CardDetails.builder()  // Changed to CardDetails
                        .setNumber(cleanCardNumber(number))
                        .setExpMonth((long) expMonth)  // Direct cast to long
                        .setExpYear((long) expYear)    // Direct cast to long
                        .setCvc(cvc)
                        .build())
                .build());
    }
}