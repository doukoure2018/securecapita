package io.getarrayus.securecapita.dto;

import io.getarrayus.securecapita.enumeration.CampaignStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CampaignsDto {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private CampaignStatus status;
    private Long totalSms;
    private Long id_user;
}
