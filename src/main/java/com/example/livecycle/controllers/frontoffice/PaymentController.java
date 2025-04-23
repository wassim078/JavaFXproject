package com.example.livecycle.controllers.frontoffice;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import java.util.regex.Pattern;

public class PaymentController {
    @FXML private TextField cardNumberField;
    @FXML private TextField expiryField;
    @FXML private TextField cvcField;

    private CheckoutController checkoutController;
    private static final Pattern EXPIRY_REGEX = Pattern.compile("^(0[1-9]|1[0-2])/?([0-9]{2})$");
    private static final Pattern CARD_REGEX = Pattern.compile("^\\d{13,19}$");
    private static final Pattern CVC_REGEX = Pattern.compile("^\\d{3,4}$");

    public void initData(CheckoutController controller, double amount) {
        this.checkoutController = controller;
        setupInputFilters();
    }

    @FXML
    private void handlePayment() {
        try {
            validateInputs();
            processPayment();
            closeWindow();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    private void validateInputs() {
        validateCardNumber();
        validateExpiry();
        validateCvc();
    }

    private void validateCardNumber() {
        String cleaned = cardNumberField.getText().replaceAll("[^0-9]", "");
        if (cleaned.length() < 13 || cleaned.length() > 19) {
            throw new IllegalArgumentException("Card number must be 13-19 digits");
        }
        if (!cleaned.matches("^4[0-9]{12}(?:[0-9]{3})?$")) { // Visa validation
            throw new IllegalArgumentException("We only accept Visa cards");
        }
    }

    private void validateExpiry() {
        if (expiryField.getText() == null || !EXPIRY_REGEX.matcher(expiryField.getText()).matches()) {
            throw new IllegalArgumentException("Invalid expiry format (MM/YY required)");
        }
    }

    private void validateCvc() {
        if (!CVC_REGEX.matcher(cvcField.getText()).matches()) {
            throw new IllegalArgumentException("Invalid CVC (3-4 digits required)");
        }
    }

    private void processPayment() {
        String cardNumber = cardNumberField.getText();
        String expiry = expiryField.getText();
        String cvc = cvcField.getText();

        if (cardNumber == null || expiry == null || cvc == null) {
            throw new IllegalArgumentException("Payment fields cannot be empty");
        }

        String[] expiryParts = expiryField.getText().split("/");
        String month = expiryParts[0];
        String year = expiryParts[1].length() == 2 ? "20" + expiryParts[1] : expiryParts[1];

        checkoutController.processStripePayment(
                cardNumberField.getText().replaceAll("[^0-9]", ""),
                month,
                year,
                cvcField.getText()
        );
    }

    private void setupInputFilters() {
        cardNumberField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d*") ? change : null));

        expiryField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d*/?\\d*") ? change : null));

        cvcField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d*") ? change : null));
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }

    private void closeWindow() {
        ((Stage) cardNumberField.getScene().getWindow()).close();
    }
}