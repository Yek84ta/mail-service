package aut.ap.graphic;

import aut.ap.mail.*;
import aut.ap.user.*;
import aut.ap.util.HibernateUtil;
import org.hibernate.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

public class Application extends JFrame {
    private final MailService mailService;
    private final UserService userService;
    private User currentUser;
    private SidebarPanel sidebarPanel;
    private ContentPanel contentPanel;
    private JPanel loginPanel;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField nameField;
    private static final Logger logger = Logger.getLogger(Application.class.getName());

    public Application() {
        this.mailService = new MailService();
        this.userService = new UserService();

        try {
            setupLogger();
            initializeLoginScreen();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Initialization error", e);
            JOptionPane.showMessageDialog(this,
                    "Failed to initialize application",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupLogger() throws Exception {
        Logger rootLogger = Logger.getLogger("");
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        FileHandler fileHandler = new FileHandler("milou.log");
        fileHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(fileHandler);
        logger.setLevel(Level.INFO);
    }

    private void initializeLoginScreen() {
        setTitle("Milou Mail - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Milou Mail Service", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        loginPanel.add(titleLabel, gbc);

        gbc.gridy = 1;
        JLabel logoLabel = new JLabel(new ImageIcon("milou_logo.png"));
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loginPanel.add(logoLabel, gbc);

        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel loginTab = createLoginTab();
        tabbedPane.addTab("Login", loginTab);

        JPanel signupTab = createSignupTab();
        tabbedPane.addTab("Sign Up", signupTab);

        loginPanel.add(tabbedPane, gbc);

        add(loginPanel);
        setVisible(true);
    }

    private JPanel createLoginTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        emailField = new JTextField(20);
        panel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> handleLogin());
        panel.add(loginButton, gbc);

        return panel;
    }

    private JPanel createSignupTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);

        gbc.gridx = 1;
        nameField = new JTextField(20);
        panel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        JTextField signupEmailField = new JTextField(20);
        panel.add(signupEmailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        JPasswordField signupPasswordField = new JPasswordField(20);
        panel.add(signupPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        JButton signupButton = new JButton("Sign Up");
        signupButton.addActionListener(e -> handleSignup(
                nameField.getText(),
                signupEmailField.getText(),
                new String(signupPasswordField.getPassword())
        ));
        panel.add(signupButton, gbc);

        return panel;
    }

    private void handleLogin() {
        String emailInput = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String email = formatEmail(emailInput);

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both email and password",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Optional<User> user = userService.loginUser(email, password, session);
            if (user.isPresent()) {
                currentUser = user.get();
                initializeMainApplication();
                showWelcomeMessage();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid email or password",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Login error", e);
            JOptionPane.showMessageDialog(this,
                    "Error during login: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSignup(String name, String emailInput, String password) {
        String email = formatEmail(emailInput.trim());

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill all fields",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            User newUser = userService.registerUser(name, email, password);
            JOptionPane.showMessageDialog(this,
                    "Your new account is created.\nGo ahead and login!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            nameField.setText("");
            emailField.setText(email);
            passwordField.setText("");
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Registration Failed", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Registration error", e);
            JOptionPane.showMessageDialog(this,
                    "Registration failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String formatEmail(String email) {
        if (!email.contains("@")) {
            return email + "@milou.com";
        }
        return email;
    }

    private void initializeMainApplication() {
        getContentPane().removeAll();
        setTitle("Milou Mail - " + currentUser.getName());
        setSize(1000, 700);
        setLocationRelativeTo(null);

        sidebarPanel = new SidebarPanel();
        contentPanel = new ContentPanel();

        setLayout(new BorderLayout());
        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        setupSidebarActions();

        revalidate();
        repaint();
    }

    private void setupSidebarActions() {
        sidebarPanel.getComposeButton().addActionListener(e -> showComposeScreen());
        sidebarPanel.getInboxButton().addActionListener(e -> loadInbox());
        sidebarPanel.getSentButton().addActionListener(e -> loadSentMails());
        sidebarPanel.getTrashButton().addActionListener(e -> loadTrashMails());
        sidebarPanel.getLogoutButton().addActionListener(e -> logout());
    }

    private void showWelcomeMessage() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<MailDto> unreadMails = mailService.getUnreadMailDtos(currentUser, session);

            StringBuilder message = new StringBuilder();
            message.append("<html><h1>Welcome back, ").append(currentUser.getName()).append("!</h1>");

            if (!unreadMails.isEmpty()) {
                message.append("<h3>Unread Emails:</h3>");
                message.append("<p>").append(unreadMails.size()).append(" unread emails:</p>");
                message.append("<ul>");
                for (MailDto mail : unreadMails) {
                    message.append("<li>").append(mail.getSenderName())
                            .append(" - ").append(mail.getSubject())
                            .append(" (").append(mail.getCode()).append(")</li>");
                }
                message.append("</ul>");
            }

            message.append("</html>");

            JLabel welcomeLabel = new JLabel(message.toString());
            contentPanel.setContent(new JScrollPane(welcomeLabel));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading unread emails", e);
        }
    }

    private void showComposeScreen() {
        JPanel composePanel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("To (comma separated):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField toField = new JTextField(30);
        formPanel.add(toField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Subject:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField subjectField = new JTextField(30);
        formPanel.add(subjectField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Body:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        JTextArea bodyArea = new JTextArea(10, 30);
        bodyArea.setLineWrap(true);
        JScrollPane bodyScroll = new JScrollPane(bodyArea);
        formPanel.add(bodyScroll, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                String[] emails = toField.getText().split(",");
                List<User> recipients = new ArrayList<>();

                for (String email : emails) {
                    String trimmedEmail = formatEmail(email.trim());
                    Optional<User> recipient = userService.findByEmail(trimmedEmail, session);
                    if (recipient.isPresent()) {
                        recipients.add(recipient.get());
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "User with email " + trimmedEmail + " not found",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                if (recipients.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Please specify at least one recipient",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (subjectField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Please enter a subject",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Mail sentMail = mailService.sendMail(
                        currentUser,
                        recipients,
                        subjectField.getText(),
                        bodyArea.getText(),
                        session
                );

                JOptionPane.showMessageDialog(this,
                        "Successfully sent your email.\nCode: " + sentMail.getCode(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadInbox();
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error sending email", ex);
                JOptionPane.showMessageDialog(this,
                        "Failed to send mail: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        composePanel.add(formPanel, BorderLayout.CENTER);
        buttonPanel.add(sendButton);
        composePanel.add(buttonPanel, BorderLayout.SOUTH);

        contentPanel.setContent(composePanel);
    }

    private void loadInbox() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<MailDto> inboxMails = mailService.getInboxDtos(currentUser, session);
            updateMailList(inboxMails, "Inbox");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading inbox", e);
            JOptionPane.showMessageDialog(this,
                    "Failed to load inbox: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSentMails() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<MailDto> sentMails = mailService.getSentMailDtos(currentUser, session);
            updateMailList(sentMails, "Sent Mails");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading sent mails", e);
            JOptionPane.showMessageDialog(this,
                    "Failed to load sent mails: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTrashMails() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<MailDto> trashMails = mailService.getTrashMailDtos(currentUser, session);
            updateMailList(trashMails, "Trash");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading trash", e);
            JOptionPane.showMessageDialog(this,
                    "Failed to load trash: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateMailList(List<MailDto> mails, String title) {
        DefaultListModel<MailDto> listModel = new DefaultListModel<>();
        JList<MailDto> mailList = new JList<>(listModel);
        mailList.setCellRenderer(new MailListRenderer());

        mails.sort(Comparator.comparing(MailDto::getSentDate).reversed());

        for (MailDto mail : mails) {
            listModel.addElement(mail);
        }

        mailList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int index = mailList.locationToIndex(evt.getPoint());
                    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                        Optional<Mail> fullMail = mailService.getMailByCode(
                                mails.get(index).getCode(),
                                currentUser,
                                session
                        );
                        fullMail.ifPresent(mail -> showMailContent(mail));
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE, "Error loading mail details", ex);
                    }
                }
            }
        });

        // Create filter panel and buttons
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton allButton = new JButton("All");
        JButton unreadButton = new JButton("Unread");
        JButton searchButton = new JButton("Search by Code");

        allButton.addActionListener(e -> {
            if ("Inbox".equals(title)) loadInbox();
            else if ("Sent Mails".equals(title)) loadSentMails();
            else if ("Trash".equals(title)) loadTrashMails();
        });

        unreadButton.addActionListener(e -> {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                List<MailDto> unreadMails = mailService.getUnreadMailDtos(currentUser, session);
                updateMailList(unreadMails, "Unread Emails");
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error loading unread mails", ex);
            }
        });

        searchButton.addActionListener(e -> {
            String code = JOptionPane.showInputDialog(this, "Enter email code:");
            if (code != null && !code.trim().isEmpty()) {
                try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                    Optional<Mail> mail = mailService.getMailByCode(code.trim(), currentUser, session);
                    if (mail.isPresent()) {
                        if (mail.get().getRecipients().contains(currentUser) ||
                                mail.get().getSender().equals(currentUser)) {
                            showMailContent(mail.get());
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "You cannot read this email",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Email not found",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Error searching mail", ex);
                }
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(title, JLabel.CENTER), BorderLayout.NORTH);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(filterPanel, BorderLayout.WEST);
        if ("Inbox".equals(title)) {
            filterPanel.add(allButton);
            filterPanel.add(unreadButton);
            filterPanel.add(searchButton);
        } else {
            filterPanel.add(searchButton);
        }

        panel.add(topPanel, BorderLayout.CENTER);
        panel.add(new JScrollPane(mailList), BorderLayout.SOUTH);

        contentPanel.setContent(panel);
    }

    private void showMailContent(Mail mail) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try (session) {
            // Mark as read if recipient - with proper transaction handling
            if (mail.getRecipients().contains(currentUser)) {
                try {
                    session.beginTransaction();
                    mailService.markAsRead(mail.getId(), currentUser.getId(), session);
                    session.getTransaction().commit();

                    // Refresh the current view after marking as read
                    if (sidebarPanel.getInboxButton().isSelected()) {
                        loadInbox();
                    }
                } catch (Exception e) {
                    if (session.getTransaction().isActive()) {
                        session.getTransaction().rollback();
                    }
                    logger.log(Level.WARNING, "Could not mark mail as read: " + e.getMessage());
                }
            }

            // Create the mail display panel
            JPanel mailPanel = new JPanel(new BorderLayout());
            mailPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Header panel with mail metadata
            JPanel headerPanel = createMailHeaderPanel(mail);

            // Mail body
            JTextArea bodyArea = new JTextArea(mail.getBody());
            bodyArea.setEditable(false);
            bodyArea.setLineWrap(true);
            bodyArea.setWrapStyleWord(true);
            JScrollPane bodyScroll = new JScrollPane(bodyArea);

            // Action buttons panel
            JPanel buttonPanel = createMailActionButtons(mail);

            // Assemble the components
            mailPanel.add(headerPanel, BorderLayout.NORTH);
            mailPanel.add(bodyScroll, BorderLayout.CENTER);
            mailPanel.add(buttonPanel, BorderLayout.SOUTH);

            // Set the content in the main panel
            contentPanel.setContent(mailPanel);

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error showing mail content", ex);
            JOptionPane.showMessageDialog(this,
                    "Error displaying mail: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    private JPanel createMailHeaderPanel(Mail mail) {
        JPanel headerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // From field
        addHeaderField(headerPanel, gbc, 0, "From:",
                mail.getSender().getName() + " <" + mail.getSender().getEmail() + ">");

        // To field
        StringBuilder recipients = new StringBuilder();
        for (User recipient : mail.getRecipients()) {
            recipients.append(recipient.getName()).append(" <").append(recipient.getEmail()).append(">, ");
        }
        if (recipients.length() > 0) {
            recipients.setLength(recipients.length() - 2);
        }
        addHeaderField(headerPanel, gbc, 1, "To:", recipients.toString());

        // Subject field
        addHeaderField(headerPanel, gbc, 2, "Subject:", mail.getSubject());

        // Date field
        addHeaderField(headerPanel, gbc, 3, "Date:", mail.getSentDate().toString());

        // Code field
        addHeaderField(headerPanel, gbc, 4, "Code:", mail.getCode());

        return headerPanel;
    }

    private void addHeaderField(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(value), gbc);
    }

    private JPanel createMailActionButtons(Mail mail) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Reply button (only for recipients)
        if (mail.getRecipients().contains(currentUser)) {
            JButton replyButton = new JButton("Reply");
            replyButton.addActionListener(e -> showReplyScreen(mail));
            buttonPanel.add(replyButton);
        }

        // Forward button
        JButton forwardButton = new JButton("Forward");
        forwardButton.addActionListener(e -> showForwardScreen(mail));
        buttonPanel.add(forwardButton);

        // Delete button
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> handleDeleteMail(mail));
        buttonPanel.add(deleteButton);

        // Restore button (only for deleted mails)
        if (mail.isDeleted()) {
            JButton restoreButton = new JButton("Restore");
            restoreButton.addActionListener(e -> handleRestoreMail(mail));
            buttonPanel.add(restoreButton);
        }

        return buttonPanel;
    }

    private void handleDeleteMail(Mail mail) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            mailService.moveToTrash(mail.getId(), currentUser, session);
            session.getTransaction().commit();
            JOptionPane.showMessageDialog(this,
                    "Email moved to trash",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            loadInbox();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error deleting mail", ex);
            JOptionPane.showMessageDialog(this,
                    "Failed to delete mail: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRestoreMail(Mail mail) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            mailService.restoreFromTrash(mail.getId(), currentUser, session);
            session.getTransaction().commit();
            JOptionPane.showMessageDialog(this,
                    "Email restored from trash",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            loadTrashMails();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error restoring mail", ex);
            JOptionPane.showMessageDialog(this,
                    "Failed to restore mail: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showReplyScreen(Mail originalMail) {
        JPanel replyPanel = new JPanel(new BorderLayout());

        String subject = originalMail.getSubject().startsWith("Re:") ?
                originalMail.getSubject() : "Re: " + originalMail.getSubject();

        List<User> replyRecipients = new ArrayList<>();
        replyRecipients.add(originalMail.getSender());
        for (User recipient : originalMail.getRecipients()) {
            if (!recipient.equals(currentUser)) {
                replyRecipients.add(recipient);
            }
        }

        StringBuilder toText = new StringBuilder();
        for (User recipient : replyRecipients) {
            toText.append(recipient.getEmail()).append(", ");
        }
        if (toText.length() > 0) {
            toText.setLength(toText.length() - 2);
        }

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("To:"), gbc);

        gbc.gridx = 1;
        JTextField toField = new JTextField(toText.toString(), 30);
        toField.setEditable(false);
        formPanel.add(toField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Subject:"), gbc);

        gbc.gridx = 1;
        JTextField subjectField = new JTextField(subject, 30);
        subjectField.setEditable(false);
        formPanel.add(subjectField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Body:"), gbc);

        gbc.gridx = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        JTextArea bodyArea = new JTextArea(10, 30);
        bodyArea.setText("\n\n---------- Original Message ----------\n" +
                "From: " + originalMail.getSender().getName() + " <" + originalMail.getSender().getEmail() + ">\n" +
                "Date: " + originalMail.getSentDate() + "\n" +
                "Subject: " + originalMail.getSubject() + "\n\n" +
                originalMail.getBody());
        bodyArea.setLineWrap(true);
        bodyArea.setEditable(false);
        JScrollPane bodyScroll = new JScrollPane(bodyArea);
        formPanel.add(bodyScroll, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton sendButton = new JButton("Send Reply");
        sendButton.addActionListener(e -> {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Mail replyMail = mailService.replyToMail(
                        originalMail,
                        currentUser,
                        bodyArea.getText(),
                        session
                );

                JOptionPane.showMessageDialog(this,
                        "Successfully sent your reply to email " + originalMail.getCode() +
                                ".\nCode: " + replyMail.getCode(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadInbox();
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error sending reply", ex);
                JOptionPane.showMessageDialog(this,
                        "Failed to send reply: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(sendButton);

        replyPanel.add(formPanel, BorderLayout.CENTER);
        replyPanel.add(buttonPanel, BorderLayout.SOUTH);

        contentPanel.setContent(replyPanel);
    }

    private void showForwardScreen(Mail originalMail) {
        JPanel forwardPanel = new JPanel(new BorderLayout());

        String subject = originalMail.getSubject().startsWith("Fw:") ?
                originalMail.getSubject() : "Fw: " + originalMail.getSubject();

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("To (comma separated):"), gbc);

        gbc.gridx = 1;
        JTextField toField = new JTextField(30);
        formPanel.add(toField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Subject:"), gbc);

        gbc.gridx = 1;
        JTextField subjectField = new JTextField(subject, 30);
        subjectField.setEditable(false);
        formPanel.add(subjectField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Body:"), gbc);

        gbc.gridx = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        JTextArea bodyArea = new JTextArea(10, 30);
        bodyArea.setText("\n\n---------- Forwarded Message ----------\n" +
                "From: " + originalMail.getSender().getName() + " <" + originalMail.getSender().getEmail() + ">\n" +
                "Date: " + originalMail.getSentDate() + "\n" +
                "Subject: " + originalMail.getSubject() + "\n\n" +
                originalMail.getBody());
        bodyArea.setLineWrap(true);
        bodyArea.setEditable(false);
        JScrollPane bodyScroll = new JScrollPane(bodyArea);
        formPanel.add(bodyScroll, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton sendButton = new JButton("Forward");
        sendButton.addActionListener(e -> {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                String[] emails = toField.getText().split(",");
                List<User> recipients = new ArrayList<>();

                for (String email : emails) {
                    String trimmedEmail = formatEmail(email.trim());
                    Optional<User> recipient = userService.findByEmail(trimmedEmail, session);
                    if (recipient.isPresent()) {
                        recipients.add(recipient.get());
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "User with email " + trimmedEmail + " not found",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                if (recipients.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Please specify at least one recipient",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Mail forwardedMail = mailService.forwardMail(
                        originalMail,
                        currentUser,
                        recipients,
                        session
                );

                JOptionPane.showMessageDialog(this,
                        "Successfully forwarded your email.\nCode: " + forwardedMail.getCode(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadInbox();
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error forwarding mail", ex);
                JOptionPane.showMessageDialog(this,
                        "Failed to forward mail: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(sendButton);

        forwardPanel.add(formPanel, BorderLayout.CENTER);
        forwardPanel.add(buttonPanel, BorderLayout.SOUTH);

        contentPanel.setContent(forwardPanel);
    }

    private void logout() {
        currentUser = null;
        getContentPane().removeAll();
        initializeLoginScreen();
    }
}