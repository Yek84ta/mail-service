package aut.ap.graphic;

import javax.swing.*;
import java.awt.*;

public class SidebarPanel extends JPanel {
    private final JButton composeButton;
    private final JButton inboxButton;
    private final JButton sentButton;
    private final JButton trashButton;
    private final JButton logoutButton;

    public SidebarPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(200, Integer.MAX_VALUE));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(240, 240, 240));

        composeButton = createStyledButton("Compose", new Color(76, 175, 80));
        inboxButton = createStyledButton("Inbox", new Color(33, 150, 243));
        sentButton = createStyledButton("Sent", new Color(255, 152, 0));
        trashButton = createStyledButton("Trash", new Color(244, 67, 54));
        logoutButton = createStyledButton("Logout", new Color(96, 125, 139));

        add(Box.createVerticalStrut(20));
        add(composeButton);
        add(Box.createVerticalStrut(15));
        add(inboxButton);
        add(Box.createVerticalStrut(15));
        add(sentButton);
        add(Box.createVerticalStrut(15));
        add(trashButton);
        add(Box.createVerticalGlue());
        add(logoutButton);
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(180, 45));
        button.setPreferredSize(new Dimension(180, 45));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return button;
    }

    public JButton getComposeButton() { return composeButton; }
    public JButton getInboxButton() { return inboxButton; }
    public JButton getSentButton() { return sentButton; }
    public JButton getTrashButton() { return trashButton; }
    public JButton getLogoutButton() { return logoutButton; }
}