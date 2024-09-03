package io.getarrayus.securecapita.service;

import io.getarrayus.securecapita.enumeration.EventType;

public interface EventsService {

    void addEvents(EventType eventType);
}
