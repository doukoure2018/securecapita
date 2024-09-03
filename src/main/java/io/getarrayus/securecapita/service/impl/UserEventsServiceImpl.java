package io.getarrayus.securecapita.service.impl;

import io.getarrayus.securecapita.dto.UserEventsDto;
import io.getarrayus.securecapita.entity.UserEvents;
import io.getarrayus.securecapita.repository.UserEventsRepository;
import io.getarrayus.securecapita.service.UserEventsService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class UserEventsServiceImpl implements UserEventsService {

    private UserEventsRepository userEventsRepository;
    private ModelMapper mapper;

    @Override
    public List<UserEventsDto> getEventsByUserId(Long userId) {
        Collection<UserEvents> userEventsCollection=userEventsRepository.findAllByUser_Id(userId);
        return userEventsCollection.stream().map(userEvents -> mapper.map(userEvents,UserEventsDto.class))
                   .collect(Collectors.toList());
    }

    @Override
    public void addUserEvent(UserEventsDto userEventsDto) {
        UserEvents userEvents = mapper.map(userEventsDto,UserEvents.class);
        userEventsRepository.save(userEvents);
    }
}
