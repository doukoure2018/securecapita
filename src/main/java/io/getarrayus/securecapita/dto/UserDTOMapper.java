package io.getarrayus.securecapita.dto;

import io.getarrayus.securecapita.entity.Users;
import io.getarrayus.securecapita.payload.UserDto;
import org.springframework.beans.BeanUtils;

public class UserDTOMapper {

    public static UserDto fromUser(Users user) {
        UserDto userDTO = new UserDto();
        BeanUtils.copyProperties(user, userDTO);
        return userDTO;
    }

//    public static UserDTO fromUser(User user, Role role) {
//        UserDTO userDTO = new UserDTO();
//        BeanUtils.copyProperties(user, userDTO);
//        userDTO.setRoleName(role.getName());
//        userDTO.setPermissions(role.getPermission());
//        return userDTO;
//    }

    public static Users toUser(UserDto userDTO) {
        Users user = new Users();
        BeanUtils.copyProperties(userDTO, user);
        return user;
    }
}
