package io.getarrayus.securecapita.dto;

import io.getarrayus.securecapita.entity.Roles;
import io.getarrayus.securecapita.entity.Users;
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

    private final Users users;
    private final Roles roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //return stream(role.getPermission().split(",".trim())).map(SimpleGrantedAuthority::new).collect(toList());
        return AuthorityUtils.commaSeparatedStringToAuthorityList(roles.getPermission());
    }

    @Override
    public String getPassword() {
        return users.getPassword(); // Assuming UserDto has a getPassword() method
    }

    @Override
    public String getUsername() {
        return users.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return users.getNonLocked(); // Assuming UserDto has a getNonLocked() method
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Or return based on your application's logic
    }

    @Override
    public boolean isEnabled() {
        return users.getEnabled();
    }

    public UserDto getUser() {
        return fromUser(this.users, roles);
    }
}

