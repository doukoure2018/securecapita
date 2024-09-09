package io.getarrayus.securecapita.dto;

import io.getarrayus.securecapita.payload.RolesDto;
import io.getarrayus.securecapita.payload.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

import static io.getarrayus.securecapita.dto.UserDTOMapper.fromUser;

@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

    private final UserDto userDto;
    private final RolesDto rolesDto;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //return stream(role.getPermission().split(",".trim())).map(SimpleGrantedAuthority::new).collect(toList());
        return AuthorityUtils.commaSeparatedStringToAuthorityList(rolesDto.getPermission());
    }

    @Override
    public String getPassword() {
        return userDto.getPassword(); // Assuming UserDto has a getPassword() method
    }

    @Override
    public String getUsername() {
        return userDto.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return userDto.getNonLocked(); // Assuming UserDto has a getNonLocked() method
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Or return based on your application's logic
    }

    @Override
    public boolean isEnabled() {
        return userDto.getEnabled();
    }

    public UserResponse getUser() {
        return fromUser(this.userDto, rolesDto);
    }
}

