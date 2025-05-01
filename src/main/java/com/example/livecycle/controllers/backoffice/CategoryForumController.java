package com.example.livecycle.controllers.backoffice;

import com.example.livecycle.entities.CategoryForum;
import com.example.livecycle.utils.DBConnexion;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class CategoryForumController implements Initializable {

    Connection con = null;
    PreparedStatement st = null;
    ResultSet rs = null;

    @FXML
    private TextField DESCRIPTION, ID, NAME;

    @FXML
    private TableColumn<CategoryForum, Integer> Id;
    @FXML
    private TableColumn<CategoryForum, String> Name, Description;

    @FXML
    private Button btnClear, btnDelete, btnSave, btnUpdate;

    @FXML
    private TableView<CategoryForum> table;

    private ObservableList<CategoryForum> categories = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        con = DBConnexion.getCon();

        // ⚠️ Lier les colonnes aux propriétés de la classe CategoryForum
        Id.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        Name.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        Description.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());

        loadCategories();
    }

    public void loadCategories() {
        categories.clear();
        String query = "SELECT * FROM categorie_forum";
        try {
            st = con.prepareStatement(query);
            rs = st.executeQuery();
            while (rs.next()) {
                categories.add(new CategoryForum(rs.getInt("id"), rs.getString("name"), rs.getString("description")));
            }
            table.setItems(categories);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void createCategory(ActionEvent event) {
        String name = NAME.getText();
        String description = DESCRIPTION.getText();
        if (name.isEmpty() || description.isEmpty()) {
            showAlert("Erreur", "Tous les champs sont obligatoires", Alert.AlertType.ERROR);
            return;
        }

        String query = "INSERT INTO categorie_forum (name, description) VALUES (?, ?)";
        try {
            st = con.prepareStatement(query);
            st.setString(1, name);
            st.setString(2, description);
            st.executeUpdate();
            showAlert("Succès", "Catégorie ajoutée avec succès", Alert.AlertType.INFORMATION);
            loadCategories();
            clearCategory(null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void updateCategory(ActionEvent event) {
        if (ID.getText().isEmpty()) {
            showAlert("Erreur", "Sélectionnez une catégorie à modifier", Alert.AlertType.ERROR);
            return;
        }

        int id = Integer.parseInt(ID.getText());
        String name = NAME.getText();
        String description = DESCRIPTION.getText();

        String query = "UPDATE categorie_forum SET name=?, description=? WHERE id=?";
        try {
            st = con.prepareStatement(query);
            st.setString(1, name);
            st.setString(2, description);
            st.setInt(3, id);
            st.executeUpdate();
            showAlert("Succès", "Catégorie mise à jour", Alert.AlertType.INFORMATION);
            loadCategories();
            clearCategory(null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void deleteCategory(ActionEvent event) {
        if (ID.getText().isEmpty()) {
            showAlert("Erreur", "Sélectionnez une catégorie à supprimer", Alert.AlertType.ERROR);
            return;
        }

        int id = Integer.parseInt(ID.getText());
        String query = "DELETE FROM categorie_forum WHERE id=?";
        try {
            st = con.prepareStatement(query);
            st.setInt(1, id);
            st.executeUpdate();
            showAlert("Succès", "Catégorie supprimée", Alert.AlertType.INFORMATION);
            loadCategories();
            clearCategory(null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void clearCategory(ActionEvent event) {
        ID.clear();
        NAME.clear();
        DESCRIPTION.clear();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void selectCategory() {
        CategoryForum selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ID.setText(String.valueOf(selected.getId()));
            NAME.setText(selected.getName());
            DESCRIPTION.setText(selected.getDescription());
        }
    }

    // Ajoutez cette méthode dans la classe
    @FXML
    void generatePdf(ActionEvent event) {
        try {
            // Création du document
            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);

            // Préparation du contenu
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);

            // En-tête
            contentStream.beginText();
            contentStream.newLineAtOffset(100, 700);
            contentStream.showText("Liste des catégories de forum");
            contentStream.endText();

            // Contenu
            int yPosition = 680;
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            for (CategoryForum category : categories) {
                contentStream.beginText();
                contentStream.newLineAtOffset(100, yPosition);
                String line = String.format("ID: %d | Nom: %s | Description: %s",
                        category.getId(), category.getName(), category.getDescription());
                contentStream.showText(line);
                contentStream.endText();
                yPosition -= 20;
            }

            contentStream.close();

            // Sauvegarde du fichier
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fileChooser.showSaveDialog(table.getScene().getWindow());

            if (file != null) {
                document.save(file);
                showAlert("Succès", "PDF généré avec succès !", Alert.AlertType.INFORMATION);
            }

            document.close();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la génération du PDF", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void generateQrCode(ActionEvent event) {
        try {
            // Créer le contenu du QR Code
            StringBuilder qrContent = new StringBuilder("Liste des catégories:\n");
            for (CategoryForum category : categories) {
                qrContent.append(String.format("ID: %d | Nom: %s\n", category.getId(), category.getName()));
            }

            // Générer le QR Code
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    qrContent.toString(),
                    BarcodeFormat.QR_CODE,
                    300,
                    300
            );

            // Convertir en image
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            // Sauvegarder le fichier
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le QR Code");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images PNG", "*.png"));
            File file = fileChooser.showSaveDialog(table.getScene().getWindow());

            if (file != null) {
                ImageIO.write(bufferedImage, "png", file);
                showAlert("Succès", "QR Code généré avec succès !", Alert.AlertType.INFORMATION);
            }
        } catch (WriterException | IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la génération du QR Code", Alert.AlertType.ERROR);
        }
    }
}
