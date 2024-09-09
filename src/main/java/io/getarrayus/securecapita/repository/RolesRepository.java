package io.getarrayus.securecapita.repository;

import io.getarrayus.securecapita.entity.Roles;
import io.getarrayus.securecapita.payload.RolesDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RolesRepository extends JpaRepository<Roles, Long> {
    Optional<Roles> findByName(String name);

    @Query("SELECT new io.getarrayus.securecapita.payload.RolesDto(r.id, r.name, r.permission) " +
            "FROM Roles r " +
            "JOIN UserRoles ur ON ur.role.id = r.id " +
            "JOIN Users u ON u.id = ur.user.id " +
            "WHERE u.id = :userId")
    RolesDto getRolesByUserId(@Param("userId") Long userId);

}
