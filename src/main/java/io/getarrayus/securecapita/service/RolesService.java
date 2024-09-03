package io.getarrayus.securecapita.service;

import io.getarrayus.securecapita.entity.Roles;
import io.getarrayus.securecapita.payload.RolesDto;

import java.util.List;

public interface RolesService {

    RolesDto getRoleByUserId(Long id);
    List<RolesDto> getRoles();

}
