package aut.ap.mail;

import java.time.LocalDateTime;
import java.util.Objects;

public class MailDto {
    private final int id;
    private final String code;
    private final String subject;
    private final String senderName;
    private final String senderEmail;
    private final LocalDateTime sentDate;
    private boolean isRead;
    private boolean isDeleted;


    // Private constructor for builder
    private MailDto(Builder builder) {
        this.id = builder.id;
        this.code = builder.code;
        this.subject = builder.subject;
        this.senderName = builder.senderName;
        this.senderEmail = builder.senderEmail;
        this.sentDate = builder.sentDate;
        this.isRead = builder.isRead;
        this.isDeleted = builder.isDeleted;
    }

    // Getters
    public int getId() { return id; }
    public String getCode() { return code; }
    public String getSubject() { return subject; }
    public String getSenderName() { return senderName; }
    public String getSenderEmail() { return senderEmail; }
    public LocalDateTime getSentDate() { return sentDate; }
    public boolean isRead() { return isRead; }
    public boolean isDeleted() { return isDeleted; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MailDto mailDto = (MailDto) o;
        return id == mailDto.id &&
                isRead == mailDto.isRead &&
                isDeleted == mailDto.isDeleted &&
                Objects.equals(code, mailDto.code) &&
                Objects.equals(subject, mailDto.subject) &&
                Objects.equals(senderName, mailDto.senderName) &&
                Objects.equals(senderEmail, mailDto.senderEmail) &&
                Objects.equals(sentDate, mailDto.sentDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, subject, senderName, senderEmail, sentDate, isRead, isDeleted);
    }

    @Override
    public String toString() {
        return "MailDto{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", subject='" + subject + '\'' +
                ", senderName='" + senderName + '\'' +
                ", senderEmail='" + senderEmail + '\'' +
                ", sentDate=" + sentDate +
                ", isRead=" + isRead +
                ", isDeleted=" + isDeleted +
                '}';
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int id;
        private String code;
        private String subject;
        private String senderName;
        private String senderEmail;
        private LocalDateTime sentDate;
        private boolean isRead;
        private boolean isDeleted;

        private Builder() {}

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder code(String code) {
            this.code = Objects.requireNonNull(code, "Code cannot be null");
            return this;
        }

        public Builder subject(String subject) {
            this.subject = Objects.requireNonNull(subject, "Subject cannot be null");
            return this;
        }

        public Builder senderName(String senderName) {
            this.senderName = Objects.requireNonNull(senderName, "Sender name cannot be null");
            return this;
        }

        public Builder senderEmail(String senderEmail) {
            this.senderEmail = Objects.requireNonNull(senderEmail, "Sender email cannot be null");
            return this;
        }

        public Builder sentDate(LocalDateTime sentDate) {
            this.sentDate = Objects.requireNonNull(sentDate, "Sent date cannot be null");
            return this;
        }

        public Builder isRead(boolean isRead) {
            this.isRead = isRead;
            return this;
        }

        public Builder isDeleted(boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        public MailDto build() {
            return new MailDto(this);
        }



    }
}