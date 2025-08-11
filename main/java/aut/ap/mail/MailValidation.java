package aut.ap.mail;

import aut.ap.user.User;
import java.util.List;
import java.util.regex.Pattern;

public class MailValidation {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final int MAX_BODY_LENGTH = 10000;

    public static void validateSubject(String subject) throws IllegalArgumentException {
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject cannot be empty.");
        }
        if (subject.length() > 255) {
            throw new IllegalArgumentException("Subject is too long (max 255 characters).");
        }
    }

    public static void validateBody(String body) throws IllegalArgumentException {
        if (body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("Body cannot be empty.");
        }
        if (body.length() > MAX_BODY_LENGTH) {
            throw new IllegalArgumentException("Body is too long (max " + MAX_BODY_LENGTH + " characters).");
        }
    }

    public static void validateRecipients(List<User> recipients) throws IllegalArgumentException {
        if (recipients == null || recipients.isEmpty()) {
            throw new IllegalArgumentException("At least one recipient is required.");
        }
        for (User recipient : recipients) {
            if (!EMAIL_PATTERN.matcher(recipient.getEmail()).matches()) {
                throw new IllegalArgumentException("Invalid email format for recipient: " + recipient.getEmail());
            }
        }
    }

    public static void validateNotSendingToSelf(User sender, List<User> recipients) {
        if (recipients.contains(sender)) {
            throw new IllegalArgumentException("Cannot send mail to yourself.");
        }
    }
}