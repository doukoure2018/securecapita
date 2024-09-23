package io.getarrayus.securecapita.service.impl;

import io.getarrayus.securecapita.dto.UserEventsDto;
import io.getarrayus.securecapita.entity.Events;
import io.getarrayus.securecapita.entity.UserEvents;
import io.getarrayus.securecapita.entity.Users;
import io.getarrayus.securecapita.enumeration.EventType;
import io.getarrayus.securecapita.exception.ApiException;
import io.getarrayus.securecapita.payload.UserDto;
import io.getarrayus.securecapita.repository.EventsRepository;
import io.getarrayus.securecapita.repository.UserEventsRepository;
import io.getarrayus.securecapita.repository.UserRepository;
import io.getarrayus.securecapita.service.UserEventsService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserEventsServiceImpl implements UserEventsService {

    private UserEventsRepository userEventsRepository;
    private UserRepository userRepository;
    private EventsRepository eventsRepository;
    private ModelMapper mapper;

    @Override
    public List<UserEventsDto> getEventsByUserId(Long userId) {
        return userEventsRepository.findRecentEventsByUserId(userId);
    }

    @Override
    public void addUserEvent(String email, EventType eventType, String device, String ipAddress,LocalDateTime createdAt) {
        // get User by email
        Optional<Users> user = userRepository.findByEmail(email);
        // get Event by eventType
        Optional<Events> events = eventsRepository.findByType(eventType.toString());
        UserEvents userEvents = new UserEvents();
        userEvents.setUser(user.get());
        userEvents.setEvent(events.get());
        userEvents.setDevice(device);
        userEvents.setIpAddress(ipAddress);
        userEvents.setCreatedAt(LocalDateTime.now());
        userEventsRepository.save(userEvents);
    }
}
