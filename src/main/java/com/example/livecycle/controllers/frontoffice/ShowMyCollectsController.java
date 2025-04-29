package com.example.livecycle.controllers.frontoffice;

import com.example.livecycle.entities.Collect;
import com.example.livecycle.entities.User;
import com.example.livecycle.services.CollectService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import com.itextpdf.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.swing.text.Document;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javax.swing.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.BaseColor;

public class ShowMyCollectsController implements Initializable {

    @FXML private TableView<Collect> collectsTable;
    @FXML private TableColumn<Collect, String> titreColumn;
    @FXML private TableColumn<Collect, String> produitColumn;
    @FXML private TableColumn<Collect, Number> quantiteColumn;
    @FXML private TableColumn<Collect, String> lieuColumn;
    @FXML private TableColumn<Collect, String> dateDebutColumn;
    @FXML private TableColumn<Collect, String> categorieColumn;
    @FXML private TableColumn<Collect, Void> actionsColumn;

    private User currentUser;
    private final CollectService collectService = new CollectService();

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadCollects();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configureTableColumns();
        configureActionsColumn();
    }


    private void configureActionsColumn() {
        actionsColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Collect, Void> call(TableColumn<Collect, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button("Edit");
                    private final Button btnDelete = new Button("Delete");
                    private final HBox hbox = new HBox(10, btnEdit, btnDelete);

                    {
                        // Button styling
                        btnEdit.setStyle("-fx-background-color: #a3d9b1; -fx-text-fill: #2d6b4d; -fx-font-weight: bold;");
                        btnDelete.setStyle("-fx-background-color: #ffb3b3; -fx-text-fill: #cc0000; -fx-font-weight: bold;");

                        // Button actions
                        btnEdit.setOnAction((ActionEvent event) -> {
                            Collect collect = getTableView().getItems().get(getIndex());
                            handleEditCollect(collect);
                        });

                        btnDelete.setOnAction((ActionEvent event) -> {
                            Collect collect = getTableView().getItems().get(getIndex());
                            handleDeleteCollect(collect);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(hbox);
                        }
                    }
                };
            }
        });
    }



    private void configureTableColumns() {
        titreColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitre()));
        produitColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNomProduit()));
        quantiteColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQuantite()));
        lieuColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLieu()));
        dateDebutColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDateDebut().toString()));
        categorieColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategorieCollect().getNom()));
    }

    private void loadCollects() {
        try {
            if (currentUser != null) {
                collectsTable.getItems().clear();
                collectsTable.getItems().addAll(collectService.recupererParUtilisateur(currentUser.getId()));
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load collects: " + e.getMessage());
        }
    }


    private void openEditWindow(Collect collect) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/frontoffice/edit_collect.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the collect data
            EditCollectController controller = loader.getController();
            controller.setCollect(collect);
            controller.setUser(currentUser); // If you need user context

            // Create a new stage
            Stage stage = new Stage();
            stage.setTitle("Edit Collection");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh table after editing
            loadCollects();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load edit window");
        }
    }



    @FXML
    private void handleEditCollect(Collect collect) {
        // Implement edit functionality
        openEditWindow(collect);
    }

    private void handleDeleteCollect(Collect collect) {
        try {
            if (collectService.supprimer(collect.getId())) {
                collectsTable.getItems().remove(collect);
                showAlert("Success", "Collect deleted successfully!");
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to delete collect: " + e.getMessage());
        }
    }


    @FXML
    private void handleDeleteCollect(ActionEvent event) {
        Collect selected = collectsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                if (collectService.supprimer(selected.getId())) {
                    collectsTable.getItems().remove(selected);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }




    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleCreateCollect(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/livecycle/frontoffice/create_collect.fxml"));
            Parent root = loader.load();

            CreateCollectController controller = loader.getController();
            controller.setUser(currentUser);

            Stage stage = new Stage();
            stage.setTitle("Create New Collect");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadCollects(); // Refresh table after creation

        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    @FXML
    private void handleGeneratePDF(ActionEvent event) {
        if (collectsTable.getItems().isEmpty()) {
            showAlert("No Data", "There are no collects to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        // Set initial filename
        fileChooser.setInitialFileName("my-collects-report.pdf");

        File file = fileChooser.showSaveDialog(collectsTable.getScene().getWindow());

        if (file != null) {
            try {
                // 1. Create Document instance
                com.itextpdf.text.Document document = new com.itextpdf.text.Document();

                // 2. Initialize PDF Writer
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));

                // 3. Open document
                document.open();

                // 4. Add content
                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
                Paragraph title = new Paragraph("My Collects Report\n\n", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);

                // Create table with 6 columns
                PdfPTable table = new PdfPTable(6);
                table.setWidthPercentage(100);
                table.setSpacingBefore(20f);
                table.setHorizontalAlignment(Element.ALIGN_CENTER);

                // Table headers
                String[] headers = {"Titre", "Produit", "Quantité", "Lieu", "Date Début", "Catégorie"};
                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header));
                    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    cell.setPadding(5);
                    table.addCell(cell);
                }

                // Table data
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                for (Collect collect : collectsTable.getItems()) {
                    addCell(table, collect.getTitre());
                    addCell(table, collect.getNomProduit());
                    addCell(table, String.valueOf(collect.getQuantite()));
                    addCell(table, collect.getLieu());
                    addCell(table, collect.getDateDebut().format(dateFormatter));
                    addCell(table, collect.getCategorieCollect().getNom());
                }

                document.add(table);
                document.close();

                // Open the generated PDF
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                } else {
                    showAlert("Success", "PDF created at: " + file.getAbsolutePath());
                }

            } catch (DocumentException | IOException e) {
                showAlert("PDF Error", "Failed to generate PDF: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                showAlert("Error", "Unexpected error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    private void addCell(PdfPTable table, String content) {
        PdfPCell cell = new PdfPCell(new Phrase(content));
        cell.setPadding(5);
        table.addCell(cell);
    }



}