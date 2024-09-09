package io.getarrayus.securecapita.service.impl;

import io.getarrayus.securecapita.entity.Roles;
import io.getarrayus.securecapita.entity.UserRoles;
import io.getarrayus.securecapita.repository.RolesRepository;
import io.getarrayus.securecapita.repository.UserRolesRepository;
import io.getarrayus.securecapita.service.UserRolesService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class UserRolesServiceImpl implements UserRolesService {

    private UserRolesRepository userRolesRepository;
    private RolesRepository rolesRepository;
    private ModelMapper mapper;
    @Override
    public void updateUserRole(Long userId, String roleName) {
//        //SELECT_ROLE_BY_NAME_QUERY
//        Optional<Roles> role = rolesRepository.findByName(roleName);
//        Roles roles = userRolesRepository.findRolesByUserId(userId);
//        // UPDATE_USER_ROLE_QUERY
//        userRolesInfo.setRole(role.get());
//        userRolesRepository.save(userRolesInfo);
    }
}
