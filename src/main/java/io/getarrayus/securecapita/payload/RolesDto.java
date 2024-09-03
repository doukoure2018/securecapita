package io.getarrayus.securecapita.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.getarrayus.securecapita.entity.UserRoles;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(NON_DEFAULT)
public class RolesDto {
    private Long id;
    private String name;
    private String permission;
}
