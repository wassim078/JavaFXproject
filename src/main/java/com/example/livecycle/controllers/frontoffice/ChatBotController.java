package com.example.livecycle.controllers.frontoffice;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.URL;
import java.util.ResourceBundle;

import org.json.JSONArray;
import org.json.JSONObject;
public class ChatBotController implements Initializable {

    @FXML private TextArea chatArea;
    @FXML private TextField userInput;

    // Configuration DeepInfra (gratuit)
    private static final String API_KEY = "zX034VNbuuBD475RnqM7MsNW7vcwSaer";
    private static final String API_URL = "https://api.deepinfra.com/v1/openai/chat/completions";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userInput.setOnAction(e -> handleUserInput());
    }

    @FXML
    private void handleUserInput() {
        String message = userInput.getText().trim();
        if (!message.isEmpty()) {
            addToChat("Vous: " + message);
            getBotResponse(message);
            userInput.clear();
        }
    }

    private void getBotResponse(String prompt) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "meta-llama/Meta-Llama-3-8B-Instruct");

            JSONArray messages = new JSONArray();
            messages.put(new JSONObject()
                    .put("role", "user")
                    .put("content", prompt)
            );
            requestBody.put("messages", messages);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpClient.newHttpClient().sendAsync(request, BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(response -> {
                        String botResponse = new JSONObject(response)
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");

                        Platform.runLater(() ->
                                addToChat("Assistant: " + botResponse)
                        );
                    });

        } catch (Exception e) {
            Platform.runLater(() ->
                    addToChat("Assistant: Désolé, erreur de connexion 😞")
            );
        }
    }

    private void addToChat(String text) {
        chatArea.appendText(text + "\n\n");
    }
}