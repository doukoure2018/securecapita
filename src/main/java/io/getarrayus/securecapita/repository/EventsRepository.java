package io.getarrayus.securecapita.repository;

import io.getarrayus.securecapita.entity.Events;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventsRepository  extends JpaRepository<Events,Long> {


}
