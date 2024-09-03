package io.getarrayus.securecapita.service;

import io.getarrayus.securecapita.dto.UserEventsDto;
import io.getarrayus.securecapita.enumeration.EventType;

import java.util.Collection;
import java.util.List;

public interface UserEventsService {

    List<UserEventsDto> getEventsByUserId(Long userId);
    void addUserEvent(UserEventsDto userEventsDto);
}
