package io.getarrayus.securecapita.utils;

import io.getarrayus.securecapita.dto.UserPrincipal;
import io.getarrayus.securecapita.dto.UserResponse;
import io.getarrayus.securecapita.entity.Users;
import io.getarrayus.securecapita.payload.UserDto;
import io.getarrayus.securecapita.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;

public class UserUtils {
    public static UserDto getAuthenticatedUser(Authentication authentication){
        //return ((UserPrincipal) authentication.getPrincipal()).getUser();
         return ((UserDto) authentication.getPrincipal());
    }

    public static UserDto getLoggedInUser(Authentication authentication) {
        return ((UserPrincipal) authentication.getPrincipal()).getUser();
    }
}
