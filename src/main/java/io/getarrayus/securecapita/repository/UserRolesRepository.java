package io.getarrayus.securecapita.repository;

import io.getarrayus.securecapita.entity.UserRoles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRolesRepository extends JpaRepository<UserRoles,Long> {

    UserRoles findByUser_Id(Long userId);
}
