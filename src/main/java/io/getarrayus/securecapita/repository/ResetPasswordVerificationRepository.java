package io.getarrayus.securecapita.repository;

import io.getarrayus.securecapita.entity.ResetPasswordVerifications;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResetPasswordVerificationRepository extends JpaRepository<ResetPasswordVerifications,Long> {

    void deleteResetPasswordVerificationsByUserId(Long userId);
}
