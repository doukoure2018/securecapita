package io.getarrayus.securecapita.repository;

import io.getarrayus.securecapita.entity.AccountVerifications;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountVerificationsRepository extends JpaRepository<AccountVerifications,Long> {
}
