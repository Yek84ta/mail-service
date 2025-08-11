package aut.ap.user;

import aut.ap.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.Optional;

public class UserService {
    private final UserRepository userRepository;
    private final UserValidation userValidation;
    private final PasswordHasher passwordHasher;

    public UserService() {
        this.userRepository = new UserRepository();
        this.userValidation = new UserValidation();
        this.passwordHasher = new PasswordHasher();
    }

    public User registerUser(String name, String email, String password) {
        userValidation.validateEmail(email);
        userValidation.validatePassword(password);
        userValidation.validateName(name);

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                // Check if email exists
                if (userRepository.existsByEmail(email, session)) {
                    throw new IllegalArgumentException("Email already registered");
                }

                String passwordHash = passwordHasher.hashPassword(password);
                User newUser = new User(name, email, passwordHash);
                session.persist(newUser);
                tx.commit();
                return newUser;
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                throw new RuntimeException("Registration failed", e);
            }
        }
    }

    public Optional<User> loginUser(String email, String password, Session session) {
        try {
            Optional<User> userOptional = userRepository.findByEmail(email, session);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                if (passwordHasher.verifyPassword(password, user.getPasswordHash())) {
                    return Optional.of(user);
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException("Login failed", e);
        }
    }

    public Optional<User> findByEmail(String email, Session session) {
        try {
            return userRepository.findByEmail(email, session);
        } catch (Exception e) {
            throw new RuntimeException("Error finding user by email", e);
        }
    }
}