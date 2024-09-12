package io.getarrayus.securecapita.dto;

import io.getarrayus.securecapita.entity.Roles;
import io.getarrayus.securecapita.entity.Users;
import io.getarrayus.securecapita.payload.RolesDto;
import io.getarrayus.securecapita.payload.UserDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;

public class UserDTOMapper {

    public static UserDto fromUser(Users user) {
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user, userDto);
        return userDto;
    }

    public static UserDto fromUser(Users user, Roles roles) {
        UserDto userDto  = new UserDto();
        BeanUtils.copyProperties(user, userDto);
        userDto.setRoleName(roles.getName());
        userDto.setPermissions(roles.getPermission());
        return userDto;
    }

    public static Users toUser(UserDto userDto) {
         Users users = new Users();
        BeanUtils.copyProperties(userDto, users);
        return users;
    }
}
