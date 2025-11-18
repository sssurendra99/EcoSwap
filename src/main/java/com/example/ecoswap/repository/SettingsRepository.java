package com.example.ecoswap.repository;

import com.example.ecoswap.model.Settings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Settings entity
 */
@Repository
public interface SettingsRepository extends JpaRepository<Settings, Long> {

    /**
     * Find the first settings record (there should only be one)
     */
    Optional<Settings> findFirstByOrderByIdAsc();
}
