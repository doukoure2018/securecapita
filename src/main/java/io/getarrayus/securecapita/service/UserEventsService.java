package io.getarrayus.securecapita.service;

import io.getarrayus.securecapita.dto.UserEventsDto;
import io.getarrayus.securecapita.enumeration.EventType;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface UserEventsService {

    List<UserEventsDto> getEventsByUserId(Long userId);
    void addUserEvent(String email, EventType eventType, String device, String ipAddress,LocalDateTime createdAt);
}
