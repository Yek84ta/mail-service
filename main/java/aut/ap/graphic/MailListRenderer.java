package aut.ap.graphic;

import aut.ap.mail.MailDto;
import jakarta.persistence.NoResultException;
import org.hibernate.Session;

import javax.swing.*;
import java.awt.*;

public class MailListRenderer extends JPanel implements ListCellRenderer<MailDto> {
    private final JLabel subjectLabel = new JLabel();
    private final JLabel senderLabel = new JLabel();
    private final JLabel dateLabel = new JLabel();
    private final JLabel statusLabel = new JLabel();

    public MailListRenderer() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setOpaque(true);

        JPanel infoPanel = new JPanel(new BorderLayout());

        JPanel subjectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        subjectPanel.setOpaque(false);
        subjectPanel.add(statusLabel);
        subjectPanel.add(subjectLabel);

        infoPanel.add(subjectPanel, BorderLayout.NORTH);

        JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        detailsPanel.setOpaque(false);
        detailsPanel.add(senderLabel);
        detailsPanel.add(dateLabel);

        infoPanel.add(detailsPanel, BorderLayout.SOUTH);
        add(infoPanel, BorderLayout.CENTER);

        Font baseFont = new Font("Arial", Font.PLAIN, 14);
        subjectLabel.setFont(baseFont);
        senderLabel.setFont(baseFont.deriveFont(12f));
        dateLabel.setFont(baseFont.deriveFont(12f));
        statusLabel.setFont(baseFont.deriveFont(Font.BOLD, 12f));
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends MailDto> list,
                                                  MailDto mailDto,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {

        subjectLabel.setText(mailDto.getSubject());
        senderLabel.setText("From: " + mailDto.getSenderName());
        dateLabel.setText(mailDto.getSentDate().toString());

        if (!mailDto.isRead()) {
            statusLabel.setText("[NEW]");
            statusLabel.setForeground(new Color(0, 128, 0)); // Green for new
            subjectLabel.setFont(subjectLabel.getFont().deriveFont(Font.BOLD));
        } else {
            statusLabel.setText("");
            subjectLabel.setFont(subjectLabel.getFont().deriveFont(Font.PLAIN));
        }

        if (isSelected) {
            setBackground(new Color(51, 153, 255)); // Blue selection
            subjectLabel.setForeground(Color.WHITE);
            senderLabel.setForeground(Color.WHITE);
            dateLabel.setForeground(Color.WHITE);
        } else {
            setBackground(list.getBackground());
            subjectLabel.setForeground(list.getForeground());
            senderLabel.setForeground(Color.GRAY);
            dateLabel.setForeground(Color.GRAY);
        }

        return this;
    }

}