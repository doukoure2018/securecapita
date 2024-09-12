package io.getarrayus.securecapita.service.impl;

import io.getarrayus.securecapita.entity.Roles;
import io.getarrayus.securecapita.payload.RolesDto;
import io.getarrayus.securecapita.repository.RolesRepository;
import io.getarrayus.securecapita.repository.UserRolesRepository;
import io.getarrayus.securecapita.service.RolesService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class RolesServiceImpl implements RolesService {

    private UserRolesRepository userRolesRepository;
    private RolesRepository rolesRepository;
    private ModelMapper mapper;

    @Override
    public RolesDto getRoleByUserId(Long id) {
        log.info("Fetching role for user Id : "+ id);
        return rolesRepository.getRolesByUserId(id);
    }

    @Override
    public List<RolesDto> getRoles() {
        List<Roles> roles=rolesRepository.findAll();
        return roles.stream().map(roles1 -> mapper.map(roles1,RolesDto.class)).collect(Collectors.toList());
    }


}
