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
        session.createNativeQuery(
                        "INSERT INTO mail_recipients (mail_id, recipient_id, is_read) " +
                                "VALUES (:mailId, :userId, true) " +
                                "ON DUPLICATE KEY UPDATE is_read = true")
                .setParameter("mailId", mailId)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public boolean isMailRead(int mailId, int userId, Session session) {
        try {
            Boolean isRead = session.createNativeQuery(
                            "SELECT is_read FROM mail_recipients " +
                                    "WHERE mail_id = :mailId AND recipient_id = :userId", Boolean.class)
                    .setParameter("mailId", mailId)
                    .setParameter("userId", userId)
                    .uniqueResult();

            return isRead;
        } catch (NoResultException e) {
            return false;
        }
    }

    public List<Mail> findInboxForUser(User user, Session session) {
        return session.createQuery(
                        "SELECT DISTINCT m FROM Mail m JOIN m.recipients r " +
                                "WHERE r = :user AND m.isDeleted = false ORDER BY m.sentDate DESC", Mail.class)
                .setParameter("user", user)
                .getResultList();
    }

    public List<Mail> findSentForUser(User user, Session session) {
        return session.createQuery(
                        "FROM Mail WHERE sender = :user AND isDeleted = false ORDER BY sentDate DESC", Mail.class)
                .setParameter("user", user)
                .getResultList();
    }

    public List<Mail> findUnreadForUser(User user, Session session) {
        return session.createQuery(
                        "SELECT DISTINCT m FROM Mail m JOIN m.recipients r " +
                                "WHERE r = :user AND m.isDeleted = false AND " +
                                "NOT EXISTS (SELECT 1 FROM mail_recipients mr WHERE mr.mail_id = m.id AND " +
                                "mr.recipient_id = :userId AND mr.is_read = true) " +
                                "ORDER BY m.sentDate DESC", Mail.class)
                .setParameter("user", user)
                .setParameter("userId", user.getId())
                .getResultList();
    }

    public void moveToTrash(int mailId, int userId, Session session) {
        try {
            // Check if recipient exists
            Boolean isRecipient = session.createNativeQuery(
                            "SELECT 1 FROM mail_recipients " +
                                    "WHERE mail_id = :mailId AND recipient_id = :userId", Boolean.class)
                    .setParameter("mailId", mailId)
                    .setParameter("userId", userId)
                    .uniqueResult();

            if (isRecipient != null) {
                // Update recipient's deletion status
                session.createNativeQuery(
                                "UPDATE mail_recipients SET is_deleted = true, deleted_at = NOW() " +
                                        "WHERE mail_id = :mailId AND recipient_id = :userId")
                        .setParameter("mailId", mailId)
                        .setParameter("userId", userId)
                        .executeUpdate();
            }

            // Check if sender
            Boolean isSender = session.createNativeQuery(
                            "SELECT 1 FROM mails " +
                                    "WHERE id = :mailId AND sender_id = :userId", Boolean.class)
                    .setParameter("mailId", mailId)
                    .setParameter("userId", userId)
                    .uniqueResult();

            if (isSender != null) {
                // Update sender's deletion status
                session.createNativeQuery(
                                "UPDATE mails SET is_deleted_by_sender = true, sender_deleted_at = NOW() " +
                                        "WHERE id = :mailId")
                        .setParameter("mailId", mailId)
                        .executeUpdate();
            }

            // Flush changes immediately
            session.flush();
        } catch (Exception e) {
            throw new RuntimeException("Repository error moving to trash", e);
        }
    }

    public void restoreFromTrash(int mailId, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Mail mail = session.get(Mail.class, mailId);
            if (mail != null) {
                mail.setDeleted(false);
                mail.setDeletedAt(null);
                mail.setDeletedById(null);
                session.persist(mail);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public List<Mail> findTrashForUser(User user, Session session) {
        return session.createQuery(
                        "FROM Mail m WHERE m.isDeleted = true AND " +
                                "(m.sender = :user OR :user MEMBER OF m.recipients) " +
                                "ORDER BY m.deletedAt DESC", Mail.class)
                .setParameter("user", user)
                .getResultList();
    }

    public Optional<String> getMailCodeById(int mailId, Session session) {
        return Optional.ofNullable(
                session.createQuery("SELECT m.code FROM Mail m WHERE m.id = :id", String.class)
                        .setParameter("id", mailId)
                        .uniqueResult()
        );
    }
}