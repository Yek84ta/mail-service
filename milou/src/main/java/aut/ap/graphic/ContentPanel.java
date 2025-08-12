package aut.ap.graphic;

import javax.swing.*;
import java.awt.*;

public class ContentPanel extends JPanel {
    public ContentPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        showWelcomeScreen();
    }

    public void setContent(JComponent content) {
        removeAll();
        add(content, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void showWelcomeScreen() {
        JLabel welcomeLabel = new JLabel(
                "<html><center><h1>Welcome to Milou Mail</h1>" +
                        "<p>Select an option from the sidebar to begin</p></center></html>",
                SwingConstants.CENTER
        );
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        setContent(welcomeLabel);
    }
}