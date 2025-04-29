package com.example.livecycle.controllers.backoffice;

import com.example.livecycle.services.CommandeService;
import com.example.livecycle.services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class AdminDefault {
    @FXML private BarChart<String, Number> leastOrderedChart;
    @FXML private ComboBox<String> frequencyPeriodSelector;

    @FXML private ComboBox<String> reclamationPeriodSelector;
    @FXML private LineChart<String, Number> registrationChart;
    @FXML private ComboBox<String> periodSelector;
    @FXML private PieChart paymentMethodChart;
    @FXML private ComboBox<String> commandesPeriodSelector;
    @FXML private LineChart<String, Number> commandesChart;
    @FXML private BarChart<String, Number> reclamationChart;
    @FXML private VBox chartContainer;
    @FXML private PieChart roleChart;
    @FXML private ComboBox<String> collectPeriodSelector;
    @FXML private LineChart<String, Number> collectChart;
    @FXML private BarChart<String, Number> userCollectChart;
    @FXML private ComboBox<String> userCollectSort;
    @FXML private TextField maxUsers;


    private final UserService userService = new UserService();
    private final CommandeService commandeService = new CommandeService();
    @FXML
    public void initialize() {
        setupPeriodSelector();
        loadChart("monthly");
        loadRoleDistribution();
        setupCommandesPeriodSelector();
        loadCommandesChart("monthly");
        loadPaymentMethodDistribution();
        setupCollectPeriodSelector();
        loadCollectChart("monthly");
        setupUserCollectChart();

        setupReclamationPeriodSelector();
        loadReclamationChart("monthly");
        setupFrequencySelector();
        loadLeastOrderedChart();
        setupPeriodSelector();
        setupCommandesPeriodSelector();
        setupReclamationPeriodSelector();
        setupFrequencySelector(); // Doit être après l'initialisation des composants

        // Ensuite charger les données
        loadChart("monthly");
        loadRoleDistribution();
        loadCommandesChart("monthly");
        loadPaymentMethodDistribution();
        loadReclamationChart("monthly");
        loadLeastOrderedChart(5);
    }

    private void loadLeastOrderedChart() {
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


    private void setupCommandesPeriodSelector() {
        commandesPeriodSelector.setItems(FXCollections.observableArrayList(
                "Daily", "Weekly", "Monthly"
        ));
        commandesPeriodSelector.getSelectionModel().select("Monthly");

        commandesPeriodSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            loadCommandesChart(newVal.toLowerCase());
        });
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

            commandesChart.setStyle("""
            -fx-chart-background-color: #f8f9fa;
            -fx-bar-gap: 2px;
            -fx-category-gap: 20px;
        """);

            // Style des barres individuelles
            for (XYChart.Series<String, Number> series : commandesChart.getData()) {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    data.getNode().setStyle("""
                    -fx-bar-fill: #2196F3;
                    -fx-background-insets: 0;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);
                """);
                }
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

    private void setupPeriodSelector() {
        periodSelector.setItems(FXCollections.observableArrayList(
                "Daily", "Weekly", "Monthly"
        ));
        periodSelector.getSelectionModel().select("Monthly");

        periodSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            loadChart(newVal.toLowerCase());
        });
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
    private void setupReclamationPeriodSelector() {
        reclamationPeriodSelector.setItems(FXCollections.observableArrayList(
                "Daily", "Monthly", "Yearly"
        ));
        reclamationPeriodSelector.getSelectionModel().select("Monthly");

        reclamationPeriodSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            loadReclamationChart(newVal.toLowerCase());
        });
    }

    private void loadReclamationChart(String period) {
        reclamationChart.getData().clear();
        Map<String, Integer> data = new ReclamationDAO().getReclamationStats(period);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        data.forEach((date, count) ->
                series.getData().add(new XYChart.Data<>(formatDateLabel(date, period), count))
        );

        reclamationChart.getData().add(series);
        styleChart(period);
    }

    private String formatDateLabel(String dateString, String period) {
        return switch (period.toLowerCase()) {
            case "daily" -> LocalDate.parse(dateString).format(DateTimeFormatter.ofPattern("dd MMM"));
            case "monthly" -> LocalDate.parse(dateString + "-01").format(DateTimeFormatter.ofPattern("MMM yyyy"));
            case "yearly" -> dateString;
            default -> dateString;
        };
    }

    private void styleChart(String period) {
        reclamationChart.getStyleClass().removeAll("daily", "monthly", "yearly");
        reclamationChart.getStyleClass().add(period.toLowerCase());

        for (XYChart.Data<String, Number> data : reclamationChart.getData().get(0).getData()) {
            data.getNode().setStyle("-fx-bar-fill: " + getColorForPeriod(period) + ";");
        }
    }

    private String getColorForPeriod(String period) {
        return switch (period.toLowerCase()) {
            case "daily" -> "#3498db";
            case "monthly" -> "#2ecc71";
            case "yearly" -> "#9b59b6";
            default -> "#e74c3c";
        };

    }
    private void setupFrequencySelector() {
        if (frequencyPeriodSelector == null) {
            throw new IllegalStateException("frequencyPeriodSelector n'est pas injecté!");
        }

        frequencyPeriodSelector.setItems(FXCollections.observableArrayList(
                "Top 5", "Top 10", "Top 15"
        ));
        frequencyPeriodSelector.getSelectionModel().select("Top 5");

        frequencyPeriodSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            int limit = Integer.parseInt(newVal.split(" ")[1]);
            loadLeastOrderedChart(limit);
        });
    }

    // Chargement des données
    private void loadLeastOrderedChart(int limit) {
        leastOrderedChart.getData().clear();
        Map<String, Integer> data = commandeService.getLeastFrequentAnnonces(limit);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Commandes");

        data.forEach((annonceId, count) -> {
            String label = "Annonce " + annonceId;
            series.getData().add(new XYChart.Data<>(label, count));
        });

        leastOrderedChart.getData().add(series);
        styleLowFrequencyChart();
    }

    // Style personnalisé
    private void styleLowFrequencyChart() {
        leastOrderedChart.setStyle("-fx-bar-fill: #e74c3c; -fx-category-gap: 20;");

        for (XYChart.Data<String, Number> data : leastOrderedChart.getData().get(0).getData()) {
            Node node = data.getNode();
            node.setStyle("-fx-bar-fill: #c0392b; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 2, 0, 0, 1);");
        }
}


    private void setupCollectPeriodSelector() {
        collectPeriodSelector.setItems(FXCollections.observableArrayList(
                "Daily", "Weekly", "Monthly"
        ));
        collectPeriodSelector.getSelectionModel().select("Monthly");

        collectPeriodSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            loadCollectChart(newVal.toLowerCase());
        });
    }
    private void loadCollectChart(String period) {
        collectChart.getData().clear();
        Map<String, Integer> data = new CollectService().getCollectTrend(period);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Collects");

        data.forEach((key, value) ->
                series.getData().add(new XYChart.Data<>(key, value))
        );

        collectChart.getData().add(series);
        applyCollectChartStyles();
    }


    private void applyCollectChartStyles() {
        collectChart.setStyle("-fx-chart-background-color: #f8f9fa;");
        collectChart.lookup(".chart-plot-background")
                .setStyle("-fx-background-color: transparent;");

        for (XYChart.Series<String, Number> s : collectChart.getData()) {
            s.getNode().lookup(".chart-series-line")
                    .setStyle("-fx-stroke: #4CAF50; -fx-stroke-width: 2px;");
        }
    }


    private void setupUserCollectChart() {
        userCollectSort.setItems(FXCollections.observableArrayList(
                "Most Collects", "Least Collects", "Alphabetical"
        ));
        userCollectSort.getSelectionModel().selectFirst();
        updateUserCollectChart();
    }

    private void styleUserCollectChart() {
        userCollectChart.setStyle("-fx-chart-background-color: #f8f9fa;");
        userCollectChart.lookup(".chart-plot-background")
                .setStyle("-fx-background-color: transparent;");

        if (!userCollectChart.getData().isEmpty()) {
            for (XYChart.Data<String, Number> data : userCollectChart.getData().get(0).getData()) {
                Node node = data.getNode();

                // Optional: if you want every bar a single custom color, uncomment this:
                // node.setStyle("-fx-bar-fill: #4CAF50;");

                // attach tooltip
                Tooltip.install(node, new Tooltip(
                        data.getXValue() + "\nCollects: " + data.getYValue()
                ));
            }
        }
    }



    @FXML
    private void updateUserCollectChart() {
        userCollectChart.getData().clear();

        String sortOrder = userCollectSort.getValue();
        int limit = parseLimit(maxUsers.getText());

        Map<String, Integer> data = new CollectService().getCollectsPerUser(sortOrder, limit);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Collects");

        data.forEach((key, value) ->
                series.getData().add(new XYChart.Data<>(key, value))
        );

        userCollectChart.getData().add(series);
        styleUserCollectChart();
    }
    private int parseLimit(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 10; // default value
        }
    }

}