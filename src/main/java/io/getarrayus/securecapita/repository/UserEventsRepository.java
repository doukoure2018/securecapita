package io.getarrayus.securecapita.repository;

import io.getarrayus.securecapita.dto.UserEventsDto;
import io.getarrayus.securecapita.entity.UserEvents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserEventsRepository extends JpaRepository<UserEvents, Long> {

    @Query("SELECT new io.getarrayus.securecapita.dto.UserEventsDto(uev.id, uev.device, uev.ipAddress, ev.type, ev.description, uev.createdAt) " +
            "FROM UserEvents uev JOIN uev.event ev JOIN uev.user u " +
            "WHERE u.id = :userId " +
            "ORDER BY uev.createdAt DESC LIMIT 10")
    List<UserEventsDto> findRecentEventsByUserId(@Param("userId") Long userId);

}
