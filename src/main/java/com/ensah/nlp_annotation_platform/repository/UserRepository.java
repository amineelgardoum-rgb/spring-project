package com.ensah.nlp_annotation_platform.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.ensah.nlp_annotation_platform.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Page<User> findByDeletedFalse(Pageable pageable);
}
