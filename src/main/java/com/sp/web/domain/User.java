package com.sp.web.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data // Brings back your getters, setters, and constructors
@Document(collection = "users")
public class User implements UserDetails {

    private static final long serialVersionUID = 1L;



    private String firstname;
    private String lastname;
    private int age;

    @MongoId
    private String username;
    private String email;
    private String center;
    private String resetToken;
    private LocalDateTime resetTokenExpiry;
    @JsonProperty("isAccountNonLocked")
    private boolean isAccountNonLocked;

    @JsonIgnore
    private String password;

     private List<String> roles = new ArrayList<>();

    public User(final String username,final String password, final String email, final String firstname, final String lastname, final int age, final String center) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.age = age;
        this.center = center;
    }

    public void addRole(String role) {
        if (this.roles == null) {
            this.roles = new ArrayList<>();
        }
        this.roles.add(role);
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // If the user has no roles, give them a default
    if (this.roles == null || this.roles.isEmpty()) {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
    
    // Otherwise, convert their stored roles into GrantedAuthorities
    return this.roles.stream()
        .map(role -> {
            // Spring Security requires roles to start with "ROLE_"
            if (!role.startsWith("ROLE_")) {
                return new SimpleGrantedAuthority("ROLE_" + role);
            }
            return new SimpleGrantedAuthority(role);
        })
        .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }


    @Override
    public boolean isAccountNonExpired() {
        return true; 
    }

    @Override
    public boolean isAccountNonLocked() {
        return isAccountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
    public void setAccountNonLocked(boolean locked) {
        this.isAccountNonLocked = locked;
    }
}