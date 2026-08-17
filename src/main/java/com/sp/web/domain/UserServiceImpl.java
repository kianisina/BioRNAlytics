package com.sp.web.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sp.web.web.NotFoundException;

@Service
public class UserServiceImpl implements  UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    @Autowired
    public UserServiceImpl(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User name with Username " + username + " not found."));
    }
    @Override
    public User registerUser(User user) {
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        //user.addRole("ROLE_USER");
        //log.info("Received first: {}", user.getSpecialistService());
        LOG.info("Received first: {}", user.getFirstname());
        user.setAccountNonLocked(false);
        return userRepository.save(user);
    }
    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    @Override
    public List<User> getUsers() {
        final List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;
    }
    @Override
    public Optional<User> getUser(String id) {
        return userRepository.findById(id);
    }
    @Override
    public User updateUser(String oldID,
                           String username,
                           String password,
                           String email,
                           String firstname,
                           String lastname,
                           boolean isAccountNonLocked,
                           List<String> roles,
                           int age,
                           String center) {
        final User user = new User(username, password, email, firstname, lastname, age, center);
        List<String> prefixedRoles = roles.stream()
            .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
            .collect(Collectors.toList());
        user.setRoles(prefixedRoles);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_admin"));
        User currentUser = userRepository.findById(currentUsername).orElse(null);
        LOG.info("ciiiiiiiiir: {}",currentUser.getUsername());
        Optional<User> oldUser = getUser(oldID);
        LOG.info("user.getPassword(): {}",oldUser.get().isAccountNonLocked());
        LOG.info("user.getPassword(aaa): {}",password);
        LOG.info("user.getPassword(),", oldUser.get().isAccountNonLocked());
        user.setAccountNonLocked(oldUser.get().isAccountNonLocked());
        if (password == null || password.equals("")) {
            user.setPassword(oldUser.get().getPassword());
        } else {
            user.setPassword(passwordEncoder.encode(password));
        }
        if (isAdmin || (currentUser != null && currentUser.getUsername().equals(username))) {
            userRepository.deleteById(oldID);
            return userRepository.save(user);
        } else {
            throw new NotFoundException();
        }
    }
    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    @Override
    public Optional<User> getUserByResetToken(String resetToken) {
        return userRepository.findByResetToken(resetToken);
    }
    @Override
    public void saveUser(User user) {
        userRepository.save(user);
    }
    @Override
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
    @Override
    public Optional<User> toggleAccountLockStatus(String username, boolean isAccountNonLocked) {
        Optional<User> userOptional = userRepository.findById(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setAccountNonLocked(isAccountNonLocked);
            userRepository.save(user);
        }
        return userOptional;
    }

}
