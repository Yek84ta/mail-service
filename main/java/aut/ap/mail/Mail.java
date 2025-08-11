package aut.ap.mail;

import aut.ap.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "mails")
public class Mail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "code", unique = true, nullable = false, length = 36)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "mail_recipients",
            joinColumns = @JoinColumn(name = "mail_id"),
            inverseJoinColumns = @JoinColumn(name = "recipient_id")
    )
    private List<User> recipients = new ArrayList<>();

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "sent_date", nullable = false)
    private LocalDateTime sentDate;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by_id")
    private Integer deletedById;

    @Transient
    private Boolean readForCurrentUser;

    // Constructors
    protected Mail() {
        // Required by JPA
    }

    public Mail(String code, User sender, List<User> recipients,
                String subject, String body, LocalDateTime sentDate) {
        this.code = validateCode(code);
        this.sender = validateSender(sender);
        this.recipients = validateRecipients(recipients);
        this.subject = validateSubject(subject);
        this.body = validateBody(body);
        this.sentDate = validateSentDate(sentDate);
    }

    // Validation methods
    private String validateCode(String code) {
        return Objects.requireNonNull(code, "Code cannot be null");
    }

    private User validateSender(User sender) {
        return Objects.requireNonNull(sender, "Sender cannot be null");
    }

    private List<User> validateRecipients(List<User> recipients) {
        Objects.requireNonNull(recipients, "Recipients cannot be null");
        return new ArrayList<>(recipients);
    }

    private String validateSubject(String subject) {
        return Objects.requireNonNull(subject, "Subject cannot be null");
    }

    private String validateBody(String body) {
        return Objects.requireNonNull(body, "Body cannot be null");
    }

    private LocalDateTime validateSentDate(LocalDateTime sentDate) {
        return Objects.requireNonNull(sentDate, "Sent date cannot be null");
    }

    // Getters
    public Integer getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public User getSender() {
        return sender;
    }

    public List<User> getRecipients() {
        return Collections.unmodifiableList(recipients);
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public LocalDateTime getSentDate() {
        return sentDate;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public Integer getDeletedById() {
        return deletedById;
    }

    // Setters
    public void setSender(User sender) {
        this.sender = validateSender(sender);
    }

    public void setRecipients(List<User> recipients) {
        this.recipients = validateRecipients(recipients);
    }

    public void setSubject(String subject) {
        this.subject = validateSubject(subject);
    }

    public void setBody(String body) {
        this.body = validateBody(body);
    }

    public void setSentDate(LocalDateTime sentDate) {
        this.sentDate = validateSentDate(sentDate);
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
        this.deletedAt = isDeleted ? LocalDateTime.now() : null;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public void setDeletedById(Integer deletedById) {
        this.deletedById = deletedById;
    }

    // Business methods
    public void markAsDeleted(Integer deletedById) {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedById = deletedById;
    }

    public void restoreFromTrash() {
        this.isDeleted = false;
        this.deletedAt = null;
        this.deletedById = null;
    }

    // Equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mail mail = (Mail) o;
        return Objects.equals(id, mail.id) && Objects.equals(code, mail.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code);
    }

    @Override
    public String toString() {
        return "Mail{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", subject='" + subject + '\'' +
                ", senderId=" + (sender != null ? sender.getId() : "null") +
                ", sentDate=" + sentDate +
                ", isDeleted=" + isDeleted +
                '}';
    }
}