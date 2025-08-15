package aut.ap;

import aut.ap.graphic.Application;
import aut.ap.user.User;
import aut.ap.util.HibernateUtil;
import org.hibernate.Session;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();

            insertTestData(session);

            if(session != null && session.isOpen()) {
                session.close();
            }

            SwingUtilities.invokeLater(() -> {
                Application app = new Application();
                app.setVisible(true);
            });

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Failed to start application: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } finally {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                HibernateUtil.shutdown();
            }));
        }
    }

    private static void insertTestData(Session session) {
        try {
            Long count = (Long) session.createQuery("SELECT COUNT(*) FROM User").uniqueResult();

            if (count == 0) {
                session.beginTransaction();

                User admin = new User("Admin User", "admin@milou.com", "admin123");
                User test = new User("Test User", "test@milou.com", "test123");

                session.persist(admin);
                session.persist(test);

                session.getTransaction().commit();
            }
        } catch (Exception e) {
            if(session.getTransaction() != null && session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            throw e;
        }
    }
}