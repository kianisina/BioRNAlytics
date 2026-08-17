package com.sp.web.web;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data // Lombok automatically creates getters, setters, and a toString() method
public class UserCMD {
    
    private String username;
    private String password;
    private String email;
    private String firstname;
    private String lastname;
    private int age;
    private String center;
    private boolean isAccountNonLocked;

   
    private List<String> roles = new ArrayList<>();

    public void setRoles(List<String> roles) {
        this.roles = List.copyOf(roles);
    }
    public List<String> getRoles() {
        return List.copyOf(roles);
    }
    
}