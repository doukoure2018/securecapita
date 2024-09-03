package io.getarrayus.securecapita.payload;

import io.getarrayus.securecapita.entity.Users;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResetPasswordVerificationsDto {
    private UserDto user;
    private String url;
    private LocalDateTime expirationDate;
}
