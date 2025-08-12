package aut.ap.mail;

import aut.ap.user.User;
import aut.ap.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MailService {
    private final MailRepository mailRepository;
    private User currentUser;
    private static final Logger logger = Logger.getLogger(MailService.class.getName());

    public MailService() {
        this.mailRepository = new MailRepository();
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public Mail sendMail(User sender, List<User> recipients, String subject, String body, Session session) {
        MailValidation.validateSubject(subject);
        MailValidation.validateBody(body);
        MailValidation.validateRecipients(recipients);
        MailValidation.validateNotSendingToSelf(sender, recipients);

        Transaction tx = session.beginTransaction();
        try {
            Mail mail = new Mail(
                    generateUniqueCode(),
                    sender,
                    recipients,
                    subject,
                    body,
                    LocalDateTime.now()
            );

            session.persist(mail);
            tx.commit();
            return mail;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Failed to send mail", e);
        }
    }

    public Optional<Mail> getMailByCode(String code, User currentUser, Session session) {
        try {
            Optional<Mail> mailOpt = mailRepository.findByCode(code, session);
            if (mailOpt.isEmpty()) {
                return Optional.empty();
            }

            Mail mail = mailOpt.get();
            if (!mail.getSender().equals(currentUser) && !mail.getRecipients().contains(currentUser)) {
                throw new SecurityException("You don't have permission to access this mail.");
            }

            // Mark as read if recipient and not already read
            if (mail.getRecipients().contains(currentUser)){
                if (!isMailRead(mail.getId(), currentUser.getId(), session)) {
                    markAsRead(mail.getId(), currentUser.getId(), session);
                }
            }

            return Optional.of(mail);
        } catch (Exception e) {
            throw new RuntimeException("Error getting mail by code", e);
        }
    }

    public List<MailDto> getInboxDtos(User user, Session session) {
        try {
            return mailRepository.findInboxForUser(user, session).stream()
                    .map(mail -> convertToDto(mail, session))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error getting inbox", e);
        }
    }

    public List<MailDto> getSentMailDtos(User user, Session session) {
        try {
            return mailRepository.findSentForUser(user, session).stream()
                    .map(mail -> convertToDto(mail, session))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error getting sent mails", e);
        }
    }

    public List<MailDto> getUnreadMailDtos(User user, Session session) {
        try {
            return mailRepository.findUnreadForUser(user, session).stream()
                    .map(mail -> convertToDto(mail, session))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error getting unread mails", e);
        }
    }

    public List<MailDto> getTrashMailDtos(User user, Session session) {
        try {
            return mailRepository.findTrashForUser(user, session).stream()
                    .map(mail -> convertToDto(mail, session))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error getting trash mails", e);
        }
    }

    public boolean isMailRead(int mailId, int userId, Session session) {
        try {
            return mailRepository.isMailRead(mailId, userId, session);
        } catch (Exception e) {
            throw new RuntimeException("Error checking mail read status", e);
        }
    }

    public void markAsRead(int mailId, int userId, Session session) {
        Transaction tx = session.beginTransaction();
        try {
            mailRepository.markAsRead(mailId, userId, session);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Error marking mail as read", e);
        }
    }

    public Mail replyToMail(Mail originalMail, User replier, String replyBody, Session session) {
        String newSubject = originalMail.getSubject().startsWith("Re:") ?
                originalMail.getSubject() : "Re: " + originalMail.getSubject();

        List<User> recipients = new ArrayList<>();
        recipients.add(originalMail.getSender());
        for (User recipient : originalMail.getRecipients()) {
            if (!recipient.equals(replier)) {
                recipients.add(recipient);
            }
        }

        return sendMail(replier, recipients, newSubject, replyBody, session);
    }

    public Mail forwardMail(Mail originalMail, User sender, List<User> recipients, Session session) {
        String newSubject = originalMail.getSubject().startsWith("Fw:") ?
                originalMail.getSubject() : "Fw: " + originalMail.getSubject();

        String body = "\n\n---------- Forwarded Message ----------\n" +
                "From: " + originalMail.getSender().getName() + " <" + originalMail.getSender().getEmail() + ">\n" +
                "Date: " + originalMail.getSentDate() + "\n" +
                "Subject: " + originalMail.getSubject() + "\n\n" +
                originalMail.getBody();

        return sendMail(sender, recipients, newSubject, body, session);
    }

    public void moveToTrash(int mailId, User user, Session session) {
        try {
            // Check if transaction is active
            boolean transactionOwner = false;
            if (!session.getTransaction().isActive()) {
                session.beginTransaction();
                transactionOwner = true;
            }

            Optional<Mail> mailOpt = mailRepository.findById(mailId, session);
            if (mailOpt.isEmpty()) {
                throw new IllegalArgumentException("Mail not found.");
            }

            Mail mail = mailOpt.get();
            if (!mail.getSender().equals(user) && !mail.getRecipients().contains(user)) {
                throw new SecurityException("You don't have permission to move this mail to trash.");
            }

            mailRepository.moveToTrash(mailId, user.getId(), session);

            if (transactionOwner) {
                session.getTransaction().commit();
            }
        } catch (Exception e) {
            if (session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            throw new RuntimeException("Error moving mail to trash", e);
        }
    }

    public void restoreFromTrash(int mailId, User user, Session session) {
        Transaction tx = session.beginTransaction();
        try {
            Optional<Mail> mailOpt = mailRepository.findById(mailId, session);
            if (mailOpt.isEmpty()) {
                throw new IllegalArgumentException("Mail not found.");
            }

            Mail mail = mailOpt.get();
            if (!mail.getSender().equals(user) && !mail.getRecipients().contains(user)) {
                throw new SecurityException("You don't have permission to restore this mail.");
            }

            mailRepository.restoreFromTrash(mailId, session);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Error restoring mail from trash", e);
        }
    }

    private MailDto convertToDto(Mail mail, Session session) {
        boolean isRead = false;
        if (currentUser != null && mail.getRecipients().contains(currentUser)) {
            isRead =isMailRead(mail.getId(), currentUser.getId(), session);
        }

        return MailDto.builder()
                .id(mail.getId())
                .code(mail.getCode())
                .subject(mail.getSubject())
                .senderName(mail.getSender().getName())
                .senderEmail(mail.getSender().getEmail())
                .sentDate(mail.getSentDate())
                .isRead(isRead)
                .isDeleted(mail.isDeleted())
                .build();
    }

    private String generateUniqueCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}