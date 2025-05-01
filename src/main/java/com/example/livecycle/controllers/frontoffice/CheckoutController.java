package com.example.livecycle.controllers.frontoffice;

import com.example.livecycle.entities.Commande;
import com.example.livecycle.entities.Panier;
import com.example.livecycle.entities.User;
import com.example.livecycle.services.CommandeService;
import com.example.livecycle.services.PanierService;
import com.example.livecycle.services.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Token;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class CheckoutController {
    // Form fields
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private ChoiceBox<String> paymentMethodChoice;
    @FXML private TextArea instructionsArea;
    @FXML private Button submitButton;

    // Dependencies
    private Panier panier;
    private User currentUser;
    private Stage stage;
    private UserDashboardController dashboardController;
    private double totalAmount;

    private final StripeService stripeService = new StripeService();

    public void initialize() {
        paymentMethodChoice.setItems(FXCollections.observableArrayList(
                CommandeService.getAllowedPaymentMethods()
        ));
        paymentMethodChoice.setValue("E-paiement");
    }

    public void initData(User user, Panier panier, double total,
                         Stage stage, UserDashboardController dashController) {
        this.currentUser = user;
        this.panier = panier;
        this.totalAmount = total;
        this.stage = stage;
        this.dashboardController = dashController;
    }

    @FXML
    private void handleSubmit() {
        if (!validateForm()) {
            showError("Please fill all required fields");
            return;
        }

        submitButton.setDisable(true);

        if (paymentMethodChoice.getValue().equals("E-paiement")) {
            showPaymentForm();
        } else {
            createCommande("AWAITING_DELIVERY");
        }
    }

    private boolean validateForm() {
        return !firstNameField.getText().isEmpty() &&
                !lastNameField.getText().isEmpty() &&
                !addressField.getText().isEmpty() &&
                !phoneField.getText().isEmpty();
    }


    private void processRealPayment(int month, int year, String cardNumber, String cvc) throws StripeException {
        Token token = stripeService.createCardToken(cardNumber, month, year, cvc);
        PaymentIntent intent = stripeService.createPaymentIntent(
                (long) (totalAmount * 100),
                "USD",
                token.getId()
        );
        handlePaymentResult(intent);
    }


    public void processStripePayment(String cardNumber, String expiryMonth, String expiryYear, String cvc) {
        try {
            if (isTestCard(cardNumber)) {
                handleTestPayment();
                return;
            }

            // Create PaymentMethod instead of Token
            PaymentMethod method = stripeService.createPaymentMethod(
                    cardNumber,
                    Integer.parseInt(expiryMonth),
                    Integer.parseInt(expiryYear),
                    cvc
            );

            PaymentIntent intent = stripeService.createPaymentIntent(
                    (long) (totalAmount * 100),
                    "USD",
                    method.getId()
            );

            handlePaymentResult(intent);
        } catch (StripeException | NumberFormatException e) {
            handleStripeError((StripeException) e);
        } finally {
            submitButton.setDisable(false);
        }
    }



    private void createCommande(String etat) {
        Commande commande = new Commande();
        commande.setUserId(currentUser.getId());
        commande.setClientName(firstNameField.getText());
        commande.setClientFamilyName(lastNameField.getText());
        commande.setClientAddress(addressField.getText());
        commande.setClientPhone(phoneField.getText());
        commande.setAnnonceQuantities(panier.getItems());
        commande.setMethodePaiement(paymentMethodChoice.getValue());
        commande.setEtatCommande(etat);
        commande.setDate(LocalDateTime.now());
        commande.setInstructionSpeciale(instructionsArea.getText());
        commande.setPrixTotal(totalAmount);

        try {
            new CommandeService().ajouter(commande);
            new PanierService().clearPanier(currentUser.getId());

            showSuccess("Order created successfully!");
            closeWindow();

            if (dashboardController != null) {
                dashboardController.refreshCartCount();
            }

        } catch (SQLException e) {
            showError("Error creating order: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void handlePaymentSuccess() {
        createCommande("PAID");
        if (dashboardController != null) {
            dashboardController.refreshCartCount();
        }
    }

    private void showError(String message) {
        new Alert(AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(AlertType.INFORMATION, message, ButtonType.OK);
        alert.initOwner(stage);
        alert.showAndWait();
    }

    private void closeWindow() {
        if (stage != null) {
            stage.close();
        }
    }

    private void showPaymentForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/frontoffice/payment_form.fxml"));
            Parent root = loader.load();

            PaymentController controller = loader.getController();
            controller.initData(this, totalAmount);

            Stage paymentStage = new Stage();
            paymentStage.initModality(Modality.APPLICATION_MODAL);
            paymentStage.setScene(new Scene(root));
            paymentStage.showAndWait();

        } catch (IOException e) {
            showError("Could not load payment form");
        }
    }


    private void handleTestPayment() {
        try {
            PaymentIntent intent = stripeService.createPaymentIntent(
                    (long) (totalAmount * 100),
                    "USD",
                    "pm_card_visa" // Explicit test payment method
            );
            handlePaymentResult(intent);
        } catch (StripeException e) {
            handleStripeError(e);
        }
    }

    private void handlePaymentResult(PaymentIntent intent) {
        switch (intent.getStatus()) {
            case "succeeded":
                handlePaymentSuccess();
                break;
            case "requires_action":
                showError("3D Secure authentication required");
                break;
            default:
                showError("Payment failed: " + intent.getLastPaymentError().getMessage());
        }
    }

    private void handleStripeError(StripeException e) {
        String errorCode = e.getCode() != null ? e.getCode() : "unknown_error";
        String errorMessage = switch (errorCode) {
            case "card_declined" -> "Card declined by issuer";
            case "incorrect_cvc" -> "Invalid CVC code";
            case "expired_card" -> "Card has expired";
            default -> "Payment error: " + e.getMessage();
        };
        showError(errorMessage);
    }


    private boolean isTestCard(String cardNumber) {
        return cardNumber.startsWith("4242");
    }

}
