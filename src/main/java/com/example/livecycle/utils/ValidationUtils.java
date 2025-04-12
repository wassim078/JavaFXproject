package com.example.livecycle.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ValidationUtils {
    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_REGEX =
            Pattern.compile("^[0-9]{8}$");
    private static final Pattern PASSWORD_REGEX =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).+$");

    public static Map<String, String> validateRegistration(
            String prenom, String nom, String email,
            String password, String confirmPassword, String telephone, String adresse) {

        Map<String, String> errors = new HashMap<>();

        if (prenom.isEmpty()) errors.put("firstName", "First name is required");
        if (nom.isEmpty()) errors.put("lastName", "Last name is required");
        if (email.isEmpty()) errors.put("email", "Email is required");
        if (password.isEmpty()) errors.put("password", "Password is required");
        if (confirmPassword.isEmpty()) errors.put("confirmPassword", "Confirm password is required");
        if (adresse.isEmpty()) errors.put("address", "Address is required");
        if (telephone.isEmpty()) errors.put("phone", "Phone number is required");

        if (!password.equals(confirmPassword)) {
            errors.put("confirmPassword", "Passwords do not match");
        }

        if (!EMAIL_REGEX.matcher(email).matches()) {
            errors.put("email", "Invalid email format");
        }

        if (!PHONE_REGEX.matcher(telephone).matches()) {
            errors.put("phone", "Phone must be 8 digits");
        }
        if (!PASSWORD_REGEX.matcher(password).matches()) {
            errors.put("password", "Password must contain both letters and digits");
        }
        return errors;
    }


    public static Map<String, String> validateLogin(String email, String password) {
        Map<String, String> errors = new HashMap<>();

        if (email.isEmpty()) errors.put("email", "Email is required");
        if (password.isEmpty()) errors.put("password", "Password is required");

        if (!EMAIL_REGEX.matcher(email).matches()) {
            errors.put("email", "Invalid email format");
        }

        return errors;
    }
}
