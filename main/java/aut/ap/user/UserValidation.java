package aut.ap.user;

public class UserValidation {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";

    public boolean validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long.");
        }
        if (!password.matches(PASSWORD_REGEX)) {
            throw new IllegalArgumentException("Password must contain at least one digit, one lowercase and one uppercase letter.");
        }
        return true;
    }

    public boolean validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }
        if (!email.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        return true;
    }

    public boolean validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        return true;
    }
}