package com.example.bankcards.security;

import com.example.bankcards.entity.UserModel;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetail implements UserDetails {

    @Getter
    private final UserModel user;

    public CustomUserDetail(UserModel user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(user.getRole().getName()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return !user.getIsDeleted();
    }

    @Override
    public boolean isAccountNonLocked() {
        return !user.getIsDeleted();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !user.getIsDeleted();
    }

    @Override
    public boolean isEnabled() {
        return !user.getIsDeleted();
    }

}
