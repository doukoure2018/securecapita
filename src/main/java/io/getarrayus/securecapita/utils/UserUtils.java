package io.getarrayus.securecapita.utils;

import io.getarrayus.securecapita.dto.UserPrincipal;
import io.getarrayus.securecapita.payload.UserDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;

public class UserUtils {

    public static UserDto getAuthenticatedUser(Authentication authentication){
         return ((UserDto) authentication.getPrincipal());
    }

    public static UserDto getLoggedInUser(Authentication authentication) {
        return ((UserPrincipal) authentication.getPrincipal()).getUser();
    }
}
