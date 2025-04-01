package com.twitterclone.security

import com.fasterxml.jackson.annotation.JsonIgnore
import com.twitterclone.models.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails implements UserDetails {
    private String id
    private String username
    private String displayName

    @JsonIgnore
    private String email

    @JsonIgnore
    private String password

    private Collection<? extends GrantedAuthority> authorities

    CustomUserDetails(
            String id,
            String username,
            String email,
            String password,
            String displayName,
            Collection<? extends GrantedAuthority> authorities) {
        this.id = id
        this.username = username
        this.email = email
        this.password = password
        this.displayName = displayName
        this.authorities = authorities
    }

    /**
     * Build UserDetails from User entity
     */
    static CustomUserDetails build(User user) {
        List<GrantedAuthority> authorities = [new SimpleGrantedAuthority("ROLE_USER")]

        return new CustomUserDetails(
                user.id,
                user.username,
                user.email,
                user.password,
                user.displayName,
                authorities
        )
    }

    String getId() {
        return id
    }

    String getEmail() {
        return email
    }

    String getDisplayName() {
        return displayName
    }

    @Override
    Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities
    }

    @Override
    String getPassword() {
        return password
    }

    @Override
    String getUsername() {
        return username
    }

    @Override
    boolean isAccountNonExpired() {
        return true
    }

    @Override
    boolean isAccountNonLocked() {
        return true
    }

    @Override
    boolean isCredentialsNonExpired() {
        return true
    }

    @Override
    boolean isEnabled() {
        return true
    }

    @Override
    boolean equals(Object o) {
        if (this == o) return true
        if (o == null || getClass() != o.getClass()) return false

        CustomUserDetails user = (CustomUserDetails) o
        return id == user.id
    }
}
