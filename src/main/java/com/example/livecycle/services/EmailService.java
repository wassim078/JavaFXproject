package com.example.livecycle.services;

import com.example.livecycle.entities.Annonce;
import com.example.livecycle.entities.Commande;
import com.stripe.model.climate.Product;
import com.stripe.service.climate.ProductService;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.mail.*;
import javax.mail.internet.*;
import java.sql.SQLException;
import java.util.Properties;

public class EmailService {
    private static final String EMAIL_USER = System.getenv("EMAIL_USER");
    private static final String EMAIL_PASSWORD = System.getenv("EMAIL_PASSWORD");
    private final Properties mailProperties;

    public EmailService() {
        mailProperties = new Properties();
        mailProperties.put("mail.smtp.auth", "true");
        mailProperties.put("mail.smtp.starttls.enable", "true");
        mailProperties.put("mail.smtp.host", "smtp.gmail.com");
        mailProperties.put("mail.smtp.port", "587");
        mailProperties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        mailProperties.put("mail.smtp.ssl.protocols", "TLSv1.2");
    }



    public void sendVerificationEmail(String toEmail, String verificationLink) {
        new Thread(() -> {
            try {
                Session session = Session.getInstance(mailProperties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(EMAIL_USER, EMAIL_PASSWORD);
                    }
                });

                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(EMAIL_USER));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
                message.setSubject("Verify Your Email Address - LiveCycle");

                String htmlContent = "<html>"
                        + "<body style='font-family: Arial, sans-serif;'>"
                        + "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;'>"
                        + "<h2 style='color: #2d572c;'>Email Verification Required</h2>"
                        + "<p>Please click the button below to verify your email address:</p>"
                        + "<a href='" + verificationLink + "' style='display: inline-block; padding: 10px 20px; "
                        + "background-color: #4CAF50; color: white; text-decoration: none; border-radius: 4px;'>"
                        + "Verify Email</a>"
                        + "<p style='margin-top: 20px; color: #666;'>If you didn't create this account, please ignore this email.</p>"
                        + "</div></body></html>";

                message.setContent(htmlContent, "text/html; charset=utf-8");

                Transport.send(message);
            } catch (AuthenticationFailedException e) {
                System.err.println("Email authentication failed. Check credentials: " + e.getMessage());
            } catch (MessagingException e) {
                System.err.println("Failed to send verification email: " + e.getMessage());
            }
        }).start();
    }

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        new Thread(() -> {
            try {
                Session session = Session.getInstance(mailProperties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(EMAIL_USER, EMAIL_PASSWORD);
                    }
                });

                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(EMAIL_USER));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
                message.setSubject("Password Reset Request - LiveCycle");

                String htmlContent = "<html><body style='font-family: Arial, sans-serif;'>" +
                        "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;'>" +
                        "<h2 style='color: #2d572c;'>Password Reset</h2>" +
                        "<p>Click the button below to reset your password:</p>" +
                        "<a href='" + resetLink + "' style='display: inline-block; padding: 10px 20px; " +
                        "background-color: #4CAF50; color: white; text-decoration: none; border-radius: 4px;'>" +
                        "Reset Password</a>" +
                        "<p style='margin-top: 20px; color: #666;'>This link will expire in 1 hour.</p>" +
                        "</div></body></html>";

                message.setContent(htmlContent, "text/html; charset=utf-8");
                Transport.send(message);
            } catch (MessagingException e) {
                System.err.println("Failed to send reset email: " + e.getMessage());
            }
        }).start();
    }

    // In EmailService.java
    public void sendOrderConfirmation(String userEmail, Commande commande) {
        new Thread(() -> {
            try {
                // First check email credentials
                if (EMAIL_USER == null || EMAIL_PASSWORD == null) {
                    System.err.println("Email credentials not configured!");
                    return;
                }

                Session session = Session.getInstance(mailProperties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(EMAIL_USER, EMAIL_PASSWORD);
                    }
                });

                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(EMAIL_USER));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(userEmail));
                message.setSubject("Facture #" + commande.getId() + " - Votre Commande LiveCycle");

                // Parse annonce quantities (format: {"2":4,"3":8})
                JSONObject annonceQuantities = new JSONObject(commande.getAnnonceQuantities());
                StringBuilder productsHtml = new StringBuilder();
                AnnonceService annonceService = new AnnonceService();
                double calculatedTotal = 0;

                for (String annonceIdStr : annonceQuantities.keySet()) {
                    try {
                        int annonceId = Integer.parseInt(annonceIdStr);
                        int quantity = annonceQuantities.getInt(annonceIdStr);

                        Annonce annonce = annonceService.getById(annonceId);
                        if (annonce == null) {
                            System.err.println("Annonce not found for ID: " + annonceId);
                            continue;
                        }

                        double price = annonce.getPrix();
                        double total = price * quantity;
                        calculatedTotal += total;

                        productsHtml.append("<tr>")
                                .append("<td style='padding: 10px; border-bottom: 1px solid #ddd;'>").append(annonce.getTitre()).append("</td>")
                                .append("<td style='padding: 10px; border-bottom: 1px solid #ddd; text-align: center;'>").append(quantity).append("</td>")
                                .append("<td style='padding: 10px; border-bottom: 1px solid #ddd; text-align: right;'>").append(String.format("%.2f TND", price)).append("</td>")
                                .append("<td style='padding: 10px; border-bottom: 1px solid #ddd; text-align: right;'>").append(String.format("%.2f TND", total)).append("</td>")
                                .append("</tr>");
                    } catch (NumberFormatException | SQLException e) {
                        System.err.println("Error processing annonce ID " + annonceIdStr + ": " + e.getMessage());
                    }
                }

                // Build HTML content
                String htmlContent = "<html><body style='font-family: Arial, sans-serif; margin: 0; padding: 20px;'>"
                        + "<div style='max-width: 800px; margin: 0 auto; background: #ffffff; border-radius: 10px; padding: 30px; box-shadow: 0 0 10px rgba(0,0,0,0.1);'>"

                        // Header Section
                        + "<div style='border-bottom: 2px solid #eee; padding-bottom: 20px; margin-bottom: 30px;'>"
                        + "<h1 style='color: #2d572c; margin: 0;'>Facture #" + commande.getId() + "</h1>"
                        + "<p style='color: #666; margin: 5px 0 0 0;'>Date: " + commande.getDate().toLocalDate() + "</p>"
                        + "<p style='color: #666; margin: 0;'>Méthode de paiement: " + commande.getMethodePaiement() + "</p>"
                        + "</div>"

                        // Client Information
                        + "<div style='margin-bottom: 30px;'>"
                        + "<h3 style='color: #333; margin-bottom: 10px;'>Client:</h3>"
                        + "<p style='margin: 5px 0;'>" + commande.getClientName() + " " + commande.getClientFamilyName() + "</p>"
                        + "<p style='margin: 5px 0;'>" + commande.getClientAddress() + "</p>"
                        + "<p style='margin: 5px 0;'>Tél: " + commande.getClientPhone() + "</p>"
                        + "</div>"

                        // Products Table
                        + "<table style='width: 100%; border-collapse: collapse; margin-bottom: 30px;'>"
                        + "<thead>"
                        + "<tr style='background-color: #f8f9fa;'>"
                        + "<th style='padding: 15px; text-align: left; border-bottom: 2px solid #ddd;'>Produit</th>"
                        + "<th style='padding: 15px; text-align: center; border-bottom: 2px solid #ddd;'>Quantité</th>"
                        + "<th style='padding: 15px; text-align: right; border-bottom: 2px solid #ddd;'>Prix Unitaire</th>"
                        + "<th style='padding: 15px; text-align: right; border-bottom: 2px solid #ddd;'>Total</th>"
                        + "</tr></thead>"
                        + "<tbody>" + productsHtml.toString() + "</tbody>"
                        + "</table>"

                        // Total Section
                        + "<div style='text-align: right; padding: 20px; background-color: #f8f9fa; border-radius: 5px;'>"
                        + "<p style='font-size: 18px; margin: 0;'>"
                        + "<span style='font-weight: bold;'>Total Général:</span> "
                        + "<span style='color: #2d572c; font-weight: bold;'>" + String.format("%.2f TND", commande.getPrixTotal()) + "</span>"
                        + "</p>"
                        + "</div>"

                        // Footer
                        + "<div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; text-align: center; color: #666;'>"
                        + "<p style='margin: 5px 0;'>Merci pour votre confiance!</p>"
                        + "<p style='margin: 5px 0;'>Contact: contact@livecycle.tn | Tél: +216 70 000 000</p>"
                        + "</div>"
                        + "</div></body></html>";

                message.setContent(htmlContent, "text/html; charset=utf-8");
                Transport.send(message);
                System.out.println("Order confirmation email sent to: " + userEmail);
            } catch (Exception e) {
                System.err.println("Failed to send order confirmation: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

}