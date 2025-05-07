package com.example.livecycle.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SMSService {

    private static final String ACCOUNT_SID = System.getenv("ACCOUNT_SID1");
    private static final String AUTH_TOKEN = System.getenv("AUTH_TOKEN1");   
    private static final String FROM_NUMBER = System.getenv("FROM_NUMBER1");                         // Numéro Twilio validé

    // Initialisation statique de Twilio
    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    /**
     * Envoie un SMS à un numéro donné avec un texte donné.
     * @param toPhoneNumber Numéro de téléphone du destinataire (format local ou international)
     * @param messageText Texte du message à envoyer
     */
    public static void sendSMS(String toPhoneNumber, String messageText) {
        try {
            String formattedTo = formatPhoneNumber(toPhoneNumber);

            Message message = Message.creator(
                    new PhoneNumber(formattedTo),
                    new PhoneNumber(FROM_NUMBER),
                    messageText
            ).create();

            System.out.println("✅ SMS envoyé avec succès ! SID = " + message.getSid());

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi du SMS : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Formate un numéro tunisien pour le rendre compatible avec Twilio (+216XXXXXXXX).
     * @param phoneNumber Numéro brut saisi par l'utilisateur
     * @return Numéro formaté en E.164
     */
    private static String formatPhoneNumber(String phoneNumber) {
        String cleaned = phoneNumber.replaceAll("[^0-9]", ""); // Enlève tout sauf les chiffres

        if (cleaned.length() == 8) {
            return "+216" + cleaned;
        } else if (cleaned.startsWith("0") && cleaned.length() == 9) {
            return "+216" + cleaned.substring(1);
        } else if (cleaned.startsWith("216") && cleaned.length() == 11) {
            return "+" + cleaned;
        } else if (cleaned.startsWith("00")) {
            return "+" + cleaned.substring(2);
        } else if (cleaned.startsWith("+" )) {
            return cleaned;
        } else {
            throw new IllegalArgumentException("Numéro de téléphone invalide : " + phoneNumber);
        }
    }
}
