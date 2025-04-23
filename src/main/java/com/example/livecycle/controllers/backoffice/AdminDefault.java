package com.example.livecycle.controllers.backoffice;

import com.example.livecycle.services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import java.util.Map;

public class AdminDefault {
    @FXML private LineChart<String, Number> registrationChart;
    @FXML private ComboBox<String> periodSelector;
    @FXML private VBox chartContainer;
    @FXML private PieChart roleChart;
    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        setupPeriodSelector();
        loadChart("monthly");
        loadRoleDistribution();
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
}