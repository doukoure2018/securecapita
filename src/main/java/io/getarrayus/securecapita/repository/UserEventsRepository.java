package io.getarrayus.securecapita.repository;

import io.getarrayus.securecapita.entity.UserEvents;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserEventsRepository extends JpaRepository<UserEvents, Long> {

    List<UserEvents> findAllByUser_Id(Long userId);
}
