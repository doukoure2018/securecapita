package io.getarrayus.securecapita.service.impl;

import io.getarrayus.securecapita.entity.Roles;
import io.getarrayus.securecapita.payload.RolesDto;
import io.getarrayus.securecapita.repository.RolesRepository;
import io.getarrayus.securecapita.service.RolesService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class RolesServiceImpl implements RolesService {

    private RolesRepository rolesRepository;
    private ModelMapper mapper;

    @Override
    public RolesDto getRoleByUserId(Long id) {
        return mapper.map(rolesRepository.findById(id), RolesDto.class);
    }

    @Override
    public List<RolesDto> getRoles() {
        List<Roles> roles=rolesRepository.findAll();
        return roles.stream().map(roles1 -> mapper.map(roles1,RolesDto.class)).collect(Collectors.toList());
    }


}
