package aut.ap.mail;

import aut.ap.user.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import jakarta.persistence.NoResultException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class MailRepository {

    public Mail save(Mail mail, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.persist(mail);
            tx.commit();
            return mail;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public Optional<Mail> findByCode(String code, Session session) {
        try {
            Mail mail = session.createQuery("FROM Mail WHERE code = :code", Mail.class)
                    .setParameter("code", code)
                    .uniqueResult();
            return Optional.ofNullable(mail);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Optional<Mail> findById(int mailId, Session session) {
        Mail mail = session.get(Mail.class, mailId);
        return Optional.ofNullable(mail);
    }

    public void markAsRead(int mailId, int userId, Session session) {
        session.createQuery(
                        "UPDATE MailRecipient mr SET mr.isRead = true " +
                                "WHERE mr.mail.id = :mailId AND mr.recipient.id = :userId")
                .setParameter("mailId", mailId)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public boolean isMailRead(int mailId, int userId, Session session) {
        try {
            return session.createQuery(
                            "SELECT mr.isRead FROM MailRecipient mr " +
                                    "WHERE mr.mail.id = :mailId AND mr.recipient.id = :userId", Boolean.class)
                    .setParameter("mailId", mailId)
                    .setParameter("userId", userId)
                    .uniqueResult();
        } catch (NoResultException e) {
            return false;
        }
    }

    public List<Mail> findInboxForUser(User user, Session session) {
        return session.createQuery(
                        "SELECT DISTINCT mr.mail FROM MailRecipient mr " +
                                "WHERE mr.recipient = :user AND mr.mail.isDeleted = false " +
                                "ORDER BY mr.mail.sentDate DESC", Mail.class)
                .setParameter("user", user)
                .getResultList();
    }

    public List<Mail> findTrashForUser(User user, Session session) {
        return session.createQuery(
                        "SELECT DISTINCT mr.mail FROM MailRecipient mr " +
                                "WHERE (mr.recipient = :user AND mr.isDeleted = true) " +
                                "OR (mr.mail.sender = :user AND mr.mail.isDeleted = true) " +
                                "ORDER BY mr.mail.deletedAt DESC", Mail.class)
                .setParameter("user", user)
                .getResultList();
    }

    public List<Mail> findUnreadForUser(User user, Session session) {
        return session.createQuery(
                        "SELECT DISTINCT mr.mail FROM MailRecipient mr " +
                                "WHERE mr.recipient = :user AND mr.isRead = false " +
                                "AND mr.mail.isDeleted = false " +
                                "ORDER BY mr.mail.sentDate DESC", Mail.class)
                .setParameter("user", user)
                .getResultList();
    }

    public List<Mail> findSentForUser(User user, Session session) {
        return session.createQuery(
                        "FROM Mail WHERE sender = :user AND isDeleted = false ORDER BY sentDate DESC", Mail.class)
                .setParameter("user", user)
                .getResultList();
    }


    public void moveToTrash(int mailId, int userId, Session session) {
        try {
            Boolean isRecipient = session.createNativeQuery(
                            "SELECT 1 FROM mail_recipients " +
                                    "WHERE mail_id = :mailId AND recipient_id = :userId", Boolean.class)
                    .setParameter("mailId", mailId)
                    .setParameter("userId", userId)
                    .uniqueResult();

            if (isRecipient != null) {
                session.createNativeQuery(
                                "UPDATE mail_recipients SET is_deleted = true, deleted_at = NOW() " +
                                        "WHERE mail_id = :mailId AND recipient_id = :userId")
                        .setParameter("mailId", mailId)
                        .setParameter("userId", userId)
                        .executeUpdate();
            }

            Boolean isSender = session.createNativeQuery(
                            "SELECT 1 FROM mails " +
                                    "WHERE id = :mailId AND sender_id = :userId", Boolean.class)
                    .setParameter("mailId", mailId)
                    .setParameter("userId", userId)
                    .uniqueResult();

            if (isSender != null) {
                session.createNativeQuery(
                                "UPDATE mails SET is_deleted = true, deleted_at = NOW(), deleted_by_id = :userId " +
                                        "WHERE id = :mailId")
                        .setParameter("mailId", mailId)
                        .setParameter("userId", userId)
                        .executeUpdate();
            }

            session.flush();
        } catch (Exception e) {
            throw new RuntimeException("Repository error moving to trash", e);
        }
    }

    public void restoreFromTrash(int mailId, Session session) {
        Mail mail = session.get(Mail.class, mailId);
        if (mail != null) {
            mail.setDeleted(false);
            mail.setDeletedAt(null);
            mail.setDeletedById(null);
        }
    }


    public Optional<String> getMailCodeById(int mailId, Session session) {
        return Optional.ofNullable(
                session.createQuery("SELECT m.code FROM Mail m WHERE m.id = :id", String.class)
                        .setParameter("id", mailId)
                        .uniqueResult()
        );
    }
}