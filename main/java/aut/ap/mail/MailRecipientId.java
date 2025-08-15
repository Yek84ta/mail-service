package aut.ap.mail;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class MailRecipientId implements Serializable {
    private Integer mail;
    private Integer recipient;

    public MailRecipientId() {
    }

    public MailRecipientId(Integer mail, Integer recipient) {
        this.mail = mail;
        this.recipient = recipient;
    }

    public Integer getMail() {
        return mail;
    }

    public void setMail(Integer mail) {
        this.mail = mail;
    }

    public Integer getRecipient() {
        return recipient;
    }

    public void setRecipient(Integer recipient) {
        this.recipient = recipient;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MailRecipientId that = (MailRecipientId) o;
        return Objects.equals(mail, that.mail) && Objects.equals(recipient, that.recipient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mail, recipient);
    }
}