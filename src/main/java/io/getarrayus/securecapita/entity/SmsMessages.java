package io.getarrayus.securecapita.entity;


import io.getarrayus.securecapita.enumeration.CampaignStatus;
import io.getarrayus.securecapita.enumeration.MessageStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "sms_Messages")
public class SmsMessages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "recipient_number")
    private String recipientNumber;
    private String message;
    @Enumerated(EnumType.STRING)
    private MessageStatus status;
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaigns campaigns;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private Users users;
}
