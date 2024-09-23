package io.getarrayus.securecapita.repository;

import io.getarrayus.securecapita.entity.Events;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventsRepository  extends JpaRepository<Events,Long> {


      Optional<Events> findByType(String eventType);
}
