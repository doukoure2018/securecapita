package io.getarrayus.securecapita.service.impl;

import io.getarrayus.securecapita.entity.Events;
import io.getarrayus.securecapita.enumeration.EventType;
import io.getarrayus.securecapita.repository.EventsRepository;
import io.getarrayus.securecapita.service.EventsService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class EventsServiceImpl implements EventsService {
    private EventsRepository eventsRepository;
    private ModelMapper mapper;
    @Override
    public void addEvents(EventType eventType) {
        Events events= mapper.map(eventType,Events.class);
        eventsRepository.save(events);
    }
}
