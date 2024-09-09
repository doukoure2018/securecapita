package io.getarrayus.securecapita.dto;

import io.getarrayus.securecapita.entity.Users;
import io.getarrayus.securecapita.payload.RolesDto;
import io.getarrayus.securecapita.payload.UserDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;

public class UserDTOMapper {

    public static UserResponse fromUser(UserDto user) {
        UserResponse userDTO = new UserResponse();
        BeanUtils.copyProperties(user, userDTO);
        return userDTO;
    }

    public static UserResponse fromUser(UserDto user, RolesDto role) {
        UserResponse userDTO = new UserResponse();
        BeanUtils.copyProperties(user, userDTO);
        userDTO.setRoleName(role.getName());
        userDTO.setPermissions(role.getPermission());
        return userDTO;
    }

    public static UserDto toUser(UserResponse userResponse) {
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(userResponse, userDto);
        return userDto;
    }
}
