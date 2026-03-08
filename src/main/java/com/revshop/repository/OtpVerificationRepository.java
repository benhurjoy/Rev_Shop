package com.revshop.repository;

import com.revshop.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findByEmailAndOtpAndUsedFalse(String email, String otp);
    Optional<OtpVerification> findTopByEmailOrderByCreatedAtDesc(String email);
    void deleteByEmail(String email);
}