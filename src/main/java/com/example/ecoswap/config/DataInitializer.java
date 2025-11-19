package com.example.ecoswap.config;

import com.example.ecoswap.model.User;
import com.example.ecoswap.model.enums.Role;
import com.example.ecoswap.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * DataInitializer - Creates default admin user on application startup
 * Runs only once when no admin user exists in the database
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Default admin credentials (change password after first login!)
    private static final String ADMIN_EMAIL = "admin@ecoswap.com";
    private static final String ADMIN_PASSWORD = "admin123";  // CHANGE THIS AFTER FIRST LOGIN
    private static final String ADMIN_NAME = "Admin User";

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        createAdminUserIfNotExists();
    }

    /**
     * Creates an admin user if no admin exists in the database
     */
    private void createAdminUserIfNotExists() {
        // Check if any admin user already exists
        long adminCount = userRepository.countByRole(Role.ADMIN);

        if (adminCount == 0) {
            logger.info("No admin user found. Creating default admin user...");

            // Create admin user
            User admin = new User();
            admin.setEmail(ADMIN_EMAIL);
            admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
            admin.setFullName(ADMIN_NAME);
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);

            userRepository.save(admin);

            logger.info("========================================");
            logger.info("DEFAULT ADMIN USER CREATED SUCCESSFULLY");
            logger.info("========================================");
            logger.info("Email: {}", ADMIN_EMAIL);
            logger.info("Password: {}", ADMIN_PASSWORD);
            logger.info("========================================");
            logger.warn("IMPORTANT: Please change the admin password after first login!");
            logger.info("========================================");
        } else {
            logger.info("Admin user already exists. Skipping admin creation.");
        }
    }
}
