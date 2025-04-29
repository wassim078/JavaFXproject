package com.example.livecycle.controllers.backoffice;

import com.example.livecycle.services.CollectService;
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
import javafx.scene.layout.VBox;
import java.util.Map;

public class AdminDefault {
    @FXML private LineChart<String, Number> registrationChart;
    @FXML private ComboBox<String> periodSelector;
    @FXML private PieChart paymentMethodChart;
    @FXML private ComboBox<String> commandesPeriodSelector;
    @FXML private LineChart<String, Number> commandesChart;
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