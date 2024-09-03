package io.getarrayus.securecapita.payload;

import io.getarrayus.securecapita.entity.Users;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TwoFactorVerificationsDto {

    private Long id;
    private Users user;
    private String code;
    private LocalDateTime expirationDate;
}
