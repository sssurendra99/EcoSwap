package com.example.ecoswap.repository;

import com.example.ecoswap.model.SellerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SellerProfileRepository extends JpaRepository<SellerProfile, Long> {

    List<SellerProfile> findByStatus(String status);

    @Query("SELECT sp FROM SellerProfile sp WHERE sp.user.id = :userId")
    Optional<SellerProfile> findByUserId(@Param("userId") Long userId);
}
