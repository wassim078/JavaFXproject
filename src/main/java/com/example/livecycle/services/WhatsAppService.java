package com.example.livecycle.services;

import okhttp3.OkHttpClient;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WhatsAppService {

    private static final String TEMPLATE_URL = "https://38q3kv.api.infobip.com/whatsapp/1/message/template";
    private static final String API_KEY = "5a0454133ba1777c49e082aeabb01cef-9590c0af-5efa-4a5a-ba55-81ca0e20db00";
    private static final String SENDER_PHONE = "447860099299"; // Numéro autorisé Infobip

    public static void sendWhatsAppTemplate(String toNumber, String userName) {
        OkHttpClient client = new OkHttpClient();

        String jsonBody = String.format(
                "{ \"messages\": [ { " +
                        "\"from\": \"%s\", " +
                        "\"to\": \"%s\", " +
                        "\"messageId\": \"3ef29bb9-d855-4cc9-b175-6207303aa2ee\", " +
                        "\"content\": { " +
                        "\"templateName\": \"test_whatsapp_template_en\", " +
                        "\"templateData\": { \"body\": { \"placeholders\": [ \"%s\" ] } }, " +
                        "\"language\": \"en\" " +
                        "} } ] }",
                SENDER_PHONE,
                toNumber.startsWith("+") ? toNumber : "+" + toNumber,
                userName
        );

        RequestBody body = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(TEMPLATE_URL)
                .post(body)
                .addHeader("Authorization", "App " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("Message template envoyé avec succès : " + response.body().string());
            } else {
                System.err.println("Erreur envoi template. Code: " + response.code()
                        + " Réponse: " + response.body().string());
            }
        } catch (Exception e) {
            System.err.println("Erreur WhatsApp (template): " + e.getMessage());
            e.printStackTrace();
        }
    }
}
