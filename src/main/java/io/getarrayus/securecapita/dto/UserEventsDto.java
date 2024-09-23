package io.getarrayus.securecapita.dto;

import io.getarrayus.securecapita.entity.Events;
import io.getarrayus.securecapita.entity.Users;
import io.getarrayus.securecapita.enumeration.EventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserEventsDto {
    private Long id;
    private String device;
    private String ipAddress;
    private String eventType;
    private String description;
    private LocalDateTime createdAt;
}
