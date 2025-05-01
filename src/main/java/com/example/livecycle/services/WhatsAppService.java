package com.example.livecycle.services;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class WhatsAppService {
    private static final String BASE_URL = "https://38q3kv.api.infobip.com";
    private static final String API_KEY = "App 5a0454133ba1777c49e082aeabb01cef-9590c0af-5efa-4a5a-ba55-81ca0e20db00";
    private static final String FROM_NUMBER = "447860099299"; // Verified Infobip sender number

    public static void sendWhatsAppMessage(String toPhoneNumber, String message) {
        new Thread(() -> {
            try {
                // Clean and format Tunisian number
                String formattedTo = toPhoneNumber.replaceAll("[^0-9]", "");
                if (formattedTo.length() == 8) {
                    formattedTo = "216" + formattedTo;
                } else if (formattedTo.startsWith("0")) {
                    formattedTo = "216" + formattedTo.substring(1);
                }
                String messageId = java.util.UUID.randomUUID().toString();
                String safeMessage = message
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\r\n", "\\n")
                        .replace("\n",   "\\n");
                // Create JSON payload
                String jsonPayload = String.format(
                        "{"
                                + "\"from\":\"%s\","
                                + "\"to\":\"%s\","
                                + "\"messageId\":\"%s\","
                                + "\"content\":{\"text\":\"%s\"},"
                                + "\"callbackData\":\"reclamation-update\""
                                + "}",
                        FROM_NUMBER,
                        formattedTo,
                        messageId,
                        safeMessage
                );

                // Configure API request
                URL url = new URL(BASE_URL + "/whatsapp/1/message/text");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");

                // ✅ EXPLICIT HEADERS
                conn.setRequestProperty("Authorization", API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                // Log request details
                System.out.println("\n=== WhatsApp API Request ===");
                System.out.println("URL: " + url);
                System.out.println("Headers: " + conn.getRequestProperties());
                System.out.println("Payload: " + jsonPayload);

                // Send payload
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                // Get response
                int responseCode = conn.getResponseCode();
                String responseBody = readResponseBody(conn, responseCode);

                // Log response details
                System.out.println("\n=== WhatsApp API Response ===");
                System.out.println("Status Code: " + responseCode);
                System.out.println("Response Body: " + responseBody);

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    System.err.println("Failed to send WhatsApp message. Server response: " + responseBody);
                }

            } catch (Exception e) {
                System.err.println("Critical error in WhatsAppService: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private static String readResponseBody(HttpURLConnection conn, int responseCode) throws IOException {
        try (InputStream inputStream = responseCode >= 400 ?
                conn.getErrorStream() : conn.getInputStream()) {

            if (inputStream == null) return "No response body";

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        }
    }
}