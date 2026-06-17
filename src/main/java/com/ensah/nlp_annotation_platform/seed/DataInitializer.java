package com.ensah.nlp_annotation_platform.seed;

import com.ensah.nlp_annotation_platform.domain.Role;
import com.ensah.nlp_annotation_platform.domain.User;
import com.ensah.nlp_annotation_platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
        seedAnnotators();
    }

    private void seedAdmin() {
        if (adminPassword == null || adminPassword.isBlank()) {
            return;
        }
        if (userRepository.findByUsername(adminUsername).isPresent()) {
            return;
        }
        User admin = new User();
        admin.setFirstName("Admin");
        admin.setLastName("Admin");
        admin.setUsername(adminUsername);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setRoles(List.of(Role.ROLE_ADMIN));
        admin.setEnabled(true);
        admin.setDeleted(false);
        userRepository.save(admin);
    }

    private void seedAnnotators() {
        String[][] users = {
            {"user1", "User", "Un"},
            {"user2", "User", "Deux"},
            {"user3", "User", "Trois"}
        };
        for (String[] u : users) {
            if (userRepository.findByUsername(u[0]).isPresent()) {
                continue;
            }
            User user = new User();
            user.setFirstName(u[1]);
            user.setLastName(u[2]);
            user.setUsername(u[0]);
            user.setPasswordHash(passwordEncoder.encode(u[0]));
            user.setRoles(List.of(Role.ROLE_ANNOTATOR));
            user.setEnabled(true);
            user.setDeleted(false);
            userRepository.save(user);
        }
    }
}
