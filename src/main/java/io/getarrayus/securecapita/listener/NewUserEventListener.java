package io.getarrayus.securecapita.listener;

import io.getarrayus.securecapita.dto.UserEventsDto;
import io.getarrayus.securecapita.event.NewUserEvent;
import io.getarrayus.securecapita.payload.UserDto;
import io.getarrayus.securecapita.service.EventsService;
import io.getarrayus.securecapita.service.UserEventsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static io.getarrayus.securecapita.utils.RequestUtils.getDevice;
import static io.getarrayus.securecapita.utils.RequestUtils.getIpAddress;

@Component
@RequiredArgsConstructor
public class NewUserEventListener {
    private final UserEventsService eventService;
    private final HttpServletRequest request;

    @EventListener
    public void onNewUserEvent(NewUserEvent event) {
        eventService.addUserEvent(event.getEmail(), event.getType(), getDevice(request), getIpAddress(request), LocalDateTime.now());
    }
}
