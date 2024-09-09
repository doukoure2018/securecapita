package io.getarrayus.securecapita.security;

import io.getarrayus.securecapita.dto.UserPrincipal;
import io.getarrayus.securecapita.entity.Roles;
import io.getarrayus.securecapita.entity.Users;
import io.getarrayus.securecapita.payload.RolesDto;
import io.getarrayus.securecapita.payload.UserDto;
import io.getarrayus.securecapita.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        Users user = userRepository.findByEmail(usernameOrEmail)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));

        Set<GrantedAuthority> authorities = user
                .getUserRoles()
                .stream()
                .map((role) -> new SimpleGrantedAuthority(role.getRole().getName()))
                .collect(Collectors.toSet());

        UserDto userDto = new UserDto();
        userDto.setEmail(user.getEmail());
        userDto.setPassword(user.getPassword());
        userDto.setNonLocked(Optional.ofNullable(user.getNonLocked()).orElse(false));
        userDto.setEnabled(Optional.ofNullable(user.getEnabled()).orElse(false));

        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setAddress(user.getAddress());
        userDto.setTitle(user.getTitle());
        userDto.setUsingMfa(user.getUsingMfa());
        // Populate other fields of userDto as needed

        RolesDto rolesDto = new RolesDto();
        rolesDto.setPermission(authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")));
        // Populate other fields of rolesDto as needed
        if (!user.getUserRoles().isEmpty()) {
            Roles role = user.getUserRoles().iterator().next().getRole();
            rolesDto.setId(role.getId());
            rolesDto.setName(role.getName());
            rolesDto.setPermission(role.getPermission());
        }
        return new UserPrincipal(userDto, rolesDto);
    }
}