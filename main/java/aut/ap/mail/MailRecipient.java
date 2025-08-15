package aut.ap.mail;

import aut.ap.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "mail_recipients")
@IdClass(MailRecipientId.class)
public class MailRecipient {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mail_id", nullable = false)
    private Mail mail;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected MailRecipient() {
    }

    public MailRecipient(Mail mail, User recipient) {
        this.mail = Objects.requireNonNull(mail);
        this.recipient = Objects.requireNonNull(recipient);
    }

    public Mail getMail() {
        return mail;
    }

    public User getRecipient() {
        return recipient;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
        this.deletedAt = deleted ? LocalDateTime.now() : null;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MailRecipient that = (MailRecipient) o;
        return Objects.equals(mail, that.mail) &&
                Objects.equals(recipient, that.recipient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mail, recipient);
    }
}