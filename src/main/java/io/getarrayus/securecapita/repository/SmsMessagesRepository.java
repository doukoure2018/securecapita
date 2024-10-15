package io.getarrayus.securecapita.repository;

import io.getarrayus.securecapita.entity.SmsMessages;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmsMessagesRepository extends JpaRepository<SmsMessages,Long> {
}
