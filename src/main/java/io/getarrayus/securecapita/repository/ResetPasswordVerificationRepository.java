package io.getarrayus.securecapita.repository;

import io.getarrayus.securecapita.entity.ResetPasswordVerifications;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ResetPasswordVerificationRepository extends JpaRepository<ResetPasswordVerifications,Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM ResetPasswordVerifications r WHERE r.user.id = :userId")
    void deleteByUserId(Long userId);
}
