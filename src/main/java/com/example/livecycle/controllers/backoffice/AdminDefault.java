package com.example.livecycle.controllers.backoffice;

import com.example.livecycle.services.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Map;

public class AdminDefault {
    @FXML private LineChart<String, Number> registrationChart;
    @FXML private LineChart<String, Number> commandesChart;
    @FXML private LineChart<String, Number> annoncesTrendChart;
    @FXML private PieChart roleChart;
    @FXML private PieChart paymentMethodChart;
    @FXML private PieChart categoryDistributionChart;
    @FXML private ComboBox<String> periodSelector;
    @FXML private ComboBox<String> commandesPeriodSelector;
    @FXML private ComboBox<String> annoncesPeriodSelector;
    @FXML private Label totalAnnoncesLabel;
    @FXML private Label activeAnnoncesLabel;
    @FXML private Label avgPriceLabel;
    @FXML private Label totalCategoriesLabel;

    private final UserService userService = new UserService();
    private final CommandeService commandeService = new CommandeService();
    private final AnnonceService annonceService = new AnnonceService();
    private final CategoryAnnonceService categoryService = new CategoryAnnonceService();
    private final DecimalFormat priceFormat = new DecimalFormat("#,##0.00 TND");

    @FXML
    public void initialize() {
        setupPeriodSelectors();
        loadAllCharts();
        loadAnnonceStats();
    }

    private void setupPeriodSelectors() {
        periodSelector.setItems(FXCollections.observableArrayList(
                "Daily", "Weekly", "Monthly"
        ));
        periodSelector.getSelectionModel().select("Monthly");

        periodSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            loadChart(newVal.toLowerCase());
        });

        commandesPeriodSelector.setItems(FXCollections.observableArrayList(
                "Daily", "Weekly", "Monthly"
        ));
        commandesPeriodSelector.getSelectionModel().select("Monthly");

        commandesPeriodSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            loadCommandesChart(newVal.toLowerCase());
        });

        annoncesPeriodSelector.setItems(FXCollections.observableArrayList(
                "Daily", "Weekly", "Monthly"
        ));
        annoncesPeriodSelector.setValue("Monthly");
        annoncesPeriodSelector.setOnAction(e -> loadAnnoncesTrendChart(
                annoncesPeriodSelector.getValue().toLowerCase()
        ));
    }

    private void loadAllCharts() {
        loadChart("monthly");
        loadRoleDistribution();
        loadCommandesChart("monthly");
        loadPaymentMethodDistribution();
        loadAnnoncesTrendChart("monthly");
        loadCategoryDistribution();
    }

    private void loadAnnonceStats() {
        try {
            Map<String, Object> stats = annonceService.getGeneralStats();
            totalAnnoncesLabel.setText(String.valueOf(stats.get("total")));
            activeAnnoncesLabel.setText(String.valueOf(stats.get("active")));
            avgPriceLabel.setText(priceFormat.format(stats.get("avgPrice")));
            
            int categoriesCount = categoryService.recuperer().size();
            totalCategoriesLabel.setText(String.valueOf(categoriesCount));
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle error appropriately
        }
    }

    private void loadAnnoncesTrendChart(String period) {
        try {
            annoncesTrendChart.getData().clear();
            Map<String, Integer> data = annonceService.getAnnoncesTrend(period);

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Annonces");

            data.forEach((key, value) ->
                    series.getData().add(new XYChart.Data<>(key, value))
            );

            annoncesTrendChart.getData().add(series);
            annoncesTrendChart.getStyleClass().add("annonces-trend");
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle error appropriately
        }
    }

    private void loadCategoryDistribution() {
        try {
            Map<String, Integer> distribution = annonceService.getCategoryDistribution();
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

            int colorIndex = 0;
            for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
                PieChart.Data slice = new PieChart.Data(
                        entry.getKey() + " (" + entry.getValue() + ")",
                        entry.getValue()
                );
                
                // Apply color class
                String colorClass = "category-" + (colorIndex % 6);
                slice.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        newNode.getStyleClass().add(colorClass);
                    }
                });
                
                pieChartData.add(slice);
                colorIndex++;
            }

            categoryDistributionChart.setData(pieChartData);
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle error appropriately
        }
    }

    private void loadPaymentMethodDistribution() {
        Map<String, Integer> paymentData = commandeService.getPaymentMethodDistribution();
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        Map<String, String> paymentColors = Map.of(
                "E-paiement", "#4CAF50",
                "à la livraison", "#2196F3"
        );

        paymentData.forEach((method, count) -> {
            PieChart.Data data = new PieChart.Data(
                    method + " (" + count + ")",
                    count
            );
            pieChartData.add(data);

            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-pie-color: " + paymentColors.get(method) + ";");
                }
            });
        });

        paymentMethodChart.setData(pieChartData);
    }

    private void loadCommandesChart(String period) {
        commandesChart.getData().clear();
        Map<String, Integer> data = commandeService.getCommandesTrend(period);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Commandes");

        data.forEach((key, value) ->
                series.getData().add(new XYChart.Data<>(key, value))
        );

        commandesChart.getData().add(series);
        applyCommandesChartStyles();
    }

    private void applyCommandesChartStyles() {
        commandesChart.setStyle("-fx-chart-background-color: #f8f9fa;");
        commandesChart.lookup(".chart-plot-background")
                .setStyle("-fx-background-color: transparent;");

        // Set line color
        for (XYChart.Series<String, Number> s : commandesChart.getData()) {
            s.getNode().lookup(".chart-series-line")
                    .setStyle("-fx-stroke: #2196F3; -fx-stroke-width: 2px;");
        }
    }

    private void loadRoleDistribution() {
        Map<String, Integer> roleData = userService.getRoleDistribution();
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        // Define colors for roles
        Map<String, String> roleColors = Map.of(
                "ROLE_ADMIN", "#379623",
                "ROLE_ENTREPRISE", "#2196F3",
                "ROLE_USER", "#FF9800"
        );

        roleData.forEach((role, count) -> {
            PieChart.Data data = new PieChart.Data(
                    role.replace("ROLE_", "") + " (" + count + ")",
                    count
            );
            pieChartData.add(data);

            // Apply custom color
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-pie-color: " + roleColors.get(role) + ";");
                }
            });
        });

        roleChart.setData(pieChartData);
    }

    private void loadChart(String period) {
        registrationChart.getData().clear();
        Map<String, Integer> data = userService.getRegistrationTrend(period);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("User Registrations");

        data.forEach((key, value) ->
                series.getData().add(new XYChart.Data<>(key, value))
        );

        registrationChart.getData().add(series);
        applyChartStyles();
    }

    private void applyChartStyles() {
        registrationChart.setStyle("-fx-chart-background-color: #f8f9fa;");
        registrationChart.lookup(".chart-plot-background")
                .setStyle("-fx-background-color: transparent;");
    }
}