package io.getarrayus.securecapita.repository;

import io.getarrayus.securecapita.entity.TwoFactorVerifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TwoFactorVerificationsRepository extends JpaRepository<TwoFactorVerifications,Long> {

    void deleteById(Long id);

    @Query("SELECT CASE WHEN t.expirationDate < CURRENT_TIMESTAMP THEN true ELSE false END " +
            "FROM TwoFactorVerifications t WHERE t.code = :code")
    Boolean isCodeExpired(@Param("code") String code);

    void deleteByCode(String code);
}
