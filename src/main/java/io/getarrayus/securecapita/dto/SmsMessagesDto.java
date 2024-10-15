package io.getarrayus.securecapita.dto;

import io.getarrayus.securecapita.enumeration.MessageStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SmsMessagesDto {

    private Long id;
    private String recipientNumber;
    private String message;
    private MessageStatus status;
    private LocalDateTime sentAt;
    private Long campaign_id;
    private Long id_user;

}
