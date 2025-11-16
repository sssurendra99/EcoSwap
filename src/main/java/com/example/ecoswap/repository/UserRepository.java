package com.example.ecoswap.repository;

import com.example.ecoswap.model.User;
import com.example.ecoswap.model.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    List<User> findByRole(Role role);

    Page<User> findByRole(Role role, Pageable pageable);

    Long countByRole(Role role);

    List<User> findByFullNameContainingOrEmailContaining(String fullName, String email);
}
