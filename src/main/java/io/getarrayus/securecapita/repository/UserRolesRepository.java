package io.getarrayus.securecapita.repository;

import io.getarrayus.securecapita.entity.Roles;
import io.getarrayus.securecapita.entity.UserRoles;
import io.getarrayus.securecapita.payload.RolesDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRolesRepository extends JpaRepository<UserRoles,Long> {

    UserRoles findUserRolesByUserId(Long userId);
}
