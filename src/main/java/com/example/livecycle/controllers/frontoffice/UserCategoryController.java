package com.example.livecycle.controllers.frontoffice;

import com.example.livecycle.entities.CategoryForum;
import com.example.livecycle.utils.DBConnexion;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.scene.control.SpinnerValueFactory;
import java.time.Duration;
import javafx.util.StringConverter;
import javafx.scene.layout.HBox;
import java.time.LocalTime;
import java.time.LocalDate;
// Ajoutez ces imports
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import java.time.LocalDate;
import java.time.LocalTime;
// Ajoutez ces imports
import com.google.api.services.calendar.model.EventDateTime;
import java.time.ZoneId;
import javafx.scene.control.Spinner;
import java.time.ZoneId;

public class UserCategoryController implements Initializable {

    @FXML
    private ListView<CategoryForum> listCategories;

    // Ajoutez ces variables
    @FXML
    private ComboBox<String> cbTargetLang;


    private final Map<String, String> langCodes = new LinkedHashMap<String, String>() {{
        put("Français", "fr");
        put("Anglais", "en");
        put("Espagnol", "es");
        put("Allemand", "de");
        put("Arabe", "ar");
    }};


    @FXML
    private Button btnAccess;

    private Connection con;
    private PreparedStatement st;
    private ResultSet rs;

    private ObservableList<CategoryForum> categories = FXCollections.observableArrayList();

    // Ajoutez cette variable de classe
    private final GoogleCalendarService calendarService = new GoogleCalendarService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        con = DBConnexion.getCon();
        setupListView();
        loadCategories();

        // Désactive le bouton tant qu'aucune sélection
        btnAccess.disableProperty().bind(
                listCategories.getSelectionModel().selectedItemProperty().isNull()
        );

        // Initialisation des langues
        cbTargetLang.getItems().addAll(langCodes.keySet());
        cbTargetLang.setValue("Français");
    }

    private void setupListView() {
        listCategories.setCellFactory(param -> new javafx.scene.control.ListCell<CategoryForum>() {
            private final VBox container = new VBox();
            private final Label titleLabel = new Label();
            private final Label descriptionLabel = new Label();

            {
                container.setSpacing(5);
                container.setPadding(new Insets(15));
                container.getStyleClass().add("category-item");

                titleLabel.getStyleClass().add("category-title");
                titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

                descriptionLabel.getStyleClass().add("category-description");
                descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
                descriptionLabel.setWrapText(true);

                container.getChildren().addAll(titleLabel, descriptionLabel);
            }

            @Override
            protected void updateItem(CategoryForum category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null) {
                    setGraphic(null);
                } else {
                    titleLabel.setText(category.getName());
                    descriptionLabel.setText(category.getDescription());
                    setGraphic(container);
                }
            }
        });
    }

    public void loadCategories() {
        categories.clear();
        String query = "SELECT * FROM categorie_forum";
        try {
            st = con.prepareStatement(query);
            rs = st.executeQuery();
            while (rs.next()) {
                categories.add(new CategoryForum(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description")
                ));
            }
            listCategories.setItems(categories);
        } catch (SQLException e) {
            showAlert("Erreur Base de Données", "Échec du chargement : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void goToForum(javafx.event.ActionEvent event) {
        CategoryForum selectedCategory = listCategories.getSelectionModel().getSelectedItem();

        // Double vérification de sécurité
        if (selectedCategory == null) {
            showAlert("Sélection Requise", "Veuillez choisir une catégorie", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/frontoffice/forum.fxml"));
            Parent root = loader.load();

            ForumController forumController = loader.getController();
            forumController.setCategory(selectedCategory);

            Stage stage = new Stage();
            stage.setTitle("Forum - " + selectedCategory.getName());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            showAlert("Erreur Navigation", "Impossible d'ouvrir le forum : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void shutdown() {
        try {
            if (rs != null) rs.close();
            if (st != null) st.close();
            if (con != null) con.close();
        } catch (SQLException e) {
            System.err.println("Erreur fermeture ressources: " + e.getMessage());
        }
    }
    @FXML

    public void openChatBot(javafx.event.ActionEvent event) {
        try {


            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/frontoffice/chatbot.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Assistant Virtuel");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le chatbot", Alert.AlertType.ERROR);
        }
    }
    // Nouvelle méthode de traduction


    private String translateText(String text, String sourceLang, String targetLang) throws Exception {
        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.name());
        URL url = new URL("https://api.mymemory.translated.net/get?q=" + encodedText +
                "&langpair=" + sourceLang + "|" + targetLang);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() != 200) {
            throw new IOException("Échec de la requête : " + conn.getResponseCode());
        }

        try (InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
            JsonObject response = JsonParser.parseReader(reader).getAsJsonObject();
            return response.get("responseData")
                    .getAsJsonObject()
                    .get("translatedText")
                    .getAsString();
        }
    }
    @FXML
    public void translateCategories(javafx.event.ActionEvent event) {
        String targetLang = langCodes.get(cbTargetLang.getValue());
        if (targetLang == null) return;

        ObservableList<CategoryForum> translatedCategories = FXCollections.observableArrayList();

        for (CategoryForum category : categories) {
            try {
                String translatedName = translateText(category.getName(), "fr", targetLang);
                String translatedDesc = translateText(category.getDescription(), "fr", targetLang);
                translatedCategories.add(new CategoryForum(
                        category.getId(),
                        translatedName,
                        translatedDesc
                ));
            } catch (Exception e) {
                showAlert("Erreur", "Échec de la traduction : " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }

        listCategories.setItems(translatedCategories);
    }

    @FXML
    public void handleCreateEvent(ActionEvent event) {
        CategoryForum selectedCategory = listCategories.getSelectionModel().getSelectedItem();

        if (selectedCategory == null) {
            showAlert("Sélection Requise", "Veuillez choisir une catégorie", Alert.AlertType.WARNING);
            return;
        }

        // Configuration de la boîte de dialogue
        Dialog<Event> dialog = new Dialog<>();
        dialog.setTitle("Créer un événement");
        dialog.setHeaderText("Créer un événement pour : " + selectedCategory.getName());

        // Configuration des champs
        DatePicker datePicker = new DatePicker();
        ComboBox<String> hourCombo = new ComboBox<>();
        ComboBox<String> minuteCombo = new ComboBox<>();
        TextField titleField = new TextField();
        Spinner<Integer> durationSpinner = new Spinner<>(15, 1440, 60, 15);
        durationSpinner.setEditable(true);

        // Validation numérique
        durationSpinner.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) durationSpinner.getEditor().setText(oldVal);
        });

        // Remplissage des ComboBox
        for (int i = 0; i < 24; i++) hourCombo.getItems().add(String.format("%02d", i));
        for (int i = 0; i < 60; i += 5) minuteCombo.getItems().add(String.format("%02d", i));
        hourCombo.setValue("09");
        minuteCombo.setValue("00");
        titleField.setText(selectedCategory.getName() + " - Réunion");

        // Organisation du layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Date:"), datePicker);
        grid.addRow(1, new Label("Heure:"), new HBox(5, hourCombo, new Label("h"), minuteCombo));
        grid.addRow(2, new Label("Titre:"), titleField);
        grid.addRow(3, new Label("Durée (minutes):"), durationSpinner);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Conversion des dates
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    LocalDate date = datePicker.getValue();
                    LocalTime time = LocalTime.of(
                            Integer.parseInt(hourCombo.getValue()),
                            Integer.parseInt(minuteCombo.getValue())
                    );

                    // Création des dates avec le bon format
                    LocalDateTime startDateTime = LocalDateTime.of(date, time);
                    LocalDateTime endDateTime = startDateTime.plusMinutes(durationSpinner.getValue());

                    // Conversion pour l'API Google
                    DateTime startDate = new DateTime(
                            startDateTime.atZone(ZoneId.of("Africa/Tunis")).toInstant().toEpochMilli()
                    );
                    DateTime endDate = new DateTime(
                            endDateTime.atZone(ZoneId.of("Africa/Tunis")).toInstant().toEpochMilli()
                    );

                    Event eventItem = new Event()
                            .setSummary(titleField.getText())
                            .setDescription("Catégorie: " + selectedCategory.getName())
                            .setStart(new EventDateTime()
                                    .setDateTime(startDate)
                                    .setTimeZone("Africa/Tunis"))
                            .setEnd(new EventDateTime()
                                    .setDateTime(endDate)
                                    .setTimeZone("Africa/Tunis"));

                    return eventItem;
                } catch (Exception e) {
                    showAlert("Erreur", "Format de date invalide: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
            return null;
        });

        // Exécution
        Optional<Event> result = dialog.showAndWait();
        result.ifPresent(eventItem -> {
            try {
                calendarService.createEvent(eventItem);
                showAlert("Succès", "Événement créé dans Google Calendar!", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "Échec de création: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

}