package aut.ap.mail;

import aut.ap.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class MailValidation {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final int MAX_BODY_LENGTH = 10000;
    private static final int MAX_SUBJECT_LENGTH = 255;
    private static final int MAX_RECIPIENTS = 50;

    public static void validateSubject(String subject) {
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject cannot be empty.");
        }
        if (subject.length() > MAX_SUBJECT_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("Subject exceeds maximum length of %d characters.", MAX_SUBJECT_LENGTH)
            );
        }
    }

    public static void validateBody(String body) {
        if (body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("Body cannot be empty.");
        }
        if (body.length() > MAX_BODY_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("Body exceeds maximum length of %d characters.", MAX_BODY_LENGTH)
            );
        }
    }

    public static void validateRecipients(List<User> recipients) {
        if (recipients == null) {
            throw new IllegalArgumentException("Recipients list cannot be null.");
        }
        if (recipients.isEmpty()) {
            throw new IllegalArgumentException("At least one recipient is required.");
        }
        if (recipients.size() > MAX_RECIPIENTS) {
            throw new IllegalArgumentException(
                    String.format("Maximum of %d recipients allowed per mail.", MAX_RECIPIENTS)
            );
        }

        long distinctCount = recipients.stream().map(User::getEmail).distinct().count();
        if (distinctCount != recipients.size()) {
            throw new IllegalArgumentException("Duplicate recipients are not allowed.");
        }

        for (User recipient : recipients) {
            if (recipient == null) {
                throw new IllegalArgumentException("Recipient cannot be null.");
            }
            if (!EMAIL_PATTERN.matcher(recipient.getEmail()).matches()) {
                throw new IllegalArgumentException(
                        String.format("Invalid email format for recipient: %s", recipient.getEmail())
                );
            }
        }
    }

    public static void validateNotSendingToSelf(User sender, List<User> recipients) {
        if (sender == null) {
            throw new IllegalArgumentException("Sender cannot be null.");
        }
        if (recipients.stream().anyMatch(recipient -> recipient.equals(sender))) {
            throw new IllegalArgumentException("Cannot send mail to yourself.");
        }
    }

    public static void validateMailAttributes(String code, User sender, List<User> recipients,
                                              String subject, String body, LocalDateTime sentDate) {
        Objects.requireNonNull(code, "Mail code cannot be null");
        Objects.requireNonNull(sender, "Sender cannot be null");
        Objects.requireNonNull(sentDate, "Sent date cannot be null");

        validateSubject(subject);
        validateBody(body);
        validateRecipients(recipients);
        validateNotSendingToSelf(sender, recipients);

        if (sentDate.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Sent date cannot be in the future.");
        }
    }
}