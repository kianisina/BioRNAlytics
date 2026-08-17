package com.sp.web.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetailsService;


public interface  UserService extends UserDetailsService {
     User registerUser(User user);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> getUsers();
    Optional<User> getUser(String id);
    User updateUser(String oldID,
                    String username,
                    String password,
                    String email,
                    String firstname,
                    String lastname,
                    boolean isAccountNonLocked,
                    List<String> roles,
                    int age,
                    String center);
    Optional<User> getUserByEmail(String email);
    Optional<User> getUserByResetToken(String resetToken);
    void saveUser(User user);
    void deleteUser(String id);
    Optional<User> toggleAccountLockStatus(String username, boolean isAccountNonLocked);

}
