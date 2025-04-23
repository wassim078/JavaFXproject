package com.example.livecycle.services;

import javax.mail.*;
import javax.mail.internet.*;
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


}