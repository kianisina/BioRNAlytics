package com.sp.web.web;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sp.web.domain.EmailService;
import com.sp.web.domain.User;
import com.sp.web.domain.UserService;

import jakarta.mail.MessagingException;

@RestController
@CrossOrigin
@RequestMapping("/api/users")
public class UserController {
    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);
    private static final String UNOTFOUND = "User not found";
    private static final String NONLOCKED = "isAccountNonLocked";
    private final UserService userService;
    private final EmailService emailService;
    private User user;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    public UserController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }
    @GetMapping("/showUsers")
    public List<User> showUsers(@AuthenticationPrincipal User user) {
        List<String> roles = new ArrayList<>(user.getRoles());
        if (roles.contains("ROLE_admin")) {
            List<User> users = userService.getUsers();
            return users;
        } else {
            List<User> users = new ArrayList<>();
            users.add(user);
            return users;
        }
    }
    
    @GetMapping("/showUsers/{id:\\w+}")
    public User showUser(@PathVariable("id") final String id) {
        Optional<User> optionalUser = this.userService.getUser(id);
        if (optionalUser.isEmpty()) {
            throw new NotFoundException();
        }
        return optionalUser.get();
    }
   
    @GetMapping("/showCurrentUser")
    public String getLoggedInUser(@AuthenticationPrincipal User user) {
//        Optional<User> optionalUser = this.userService.getUser(user.getUsername());
//        if (optionalUser.isEmpty()) {
//            throw new NotFoundException();
//        }
        System.out.println(user.getEmail());
        this.user = user;
        LOG.info("Recuuuuu: {}", user.getFirstname());
        return user.getUsername();
    }
    
    @PutMapping("/updateUser/{id:\\w+}")
    public User update(@PathVariable("id") String id, @RequestBody UserCMD userCMD) {
        //log.info("Recsuuuuu: {}", user.getFirstname());
        return userService.updateUser(id,
            userCMD.getUsername(),
            userCMD.getPassword(),
            userCMD.getEmail(),
            userCMD.getFirstname(),
            userCMD.getLastname(),
            userCMD.isAccountNonLocked(),
            userCMD.getRoles(),
            userCMD.getAge(),
            userCMD.getCenter());
    }
    
    @DeleteMapping("/deleteUser/{id:\\w+}")
    public ResponseEntity<?> delete(@PathVariable("id") String id) {
        Optional<User> optionalUser = userService.getUser(id);
        LOG.info("usercurrent: {}", user.getUsername());
        LOG.info("userdelete: {}", id);
        if (user.getUsername().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin can not deleted");
        }
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(UNOTFOUND);
        }
        userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.OK).body("User deleted successfully");
    }
    @PreAuthorize("hasRole('admin')")
    @PutMapping("/updateAccountLockStatus/{username}")
    public ResponseEntity<?> updateAccountLockStatus(@PathVariable String username,
                                                     @RequestBody Map<String, Boolean> lockStatus,
                                                     @AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> loggedInUserOpt = userService.getUser(userDetails.getUsername());
    if (loggedInUserOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
    }
    User loggedInUser = loggedInUserOpt.get();
    LOG.info("User roles: {}", loggedInUser.getRoles());
    
    boolean isAccountNonLocked = lockStatus.get(NONLOCKED);
    Optional<User> userOptional = userService.toggleAccountLockStatus(username, isAccountNonLocked);
    if (userOptional.isPresent()) {
        User updatedUser = userOptional.get();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User account lock status updated successfully");
        response.put(NONLOCKED, updatedUser.isAccountNonLocked());
        return ResponseEntity.ok().body(response);
    } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(UNOTFOUND);
    }
    }
    // build a request for the user
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserCMD userCMD) {
        // Check if the username or email already exists
        if (userService.existsByUsername(userCMD.getUsername())) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body("Username already exists");
        }
        if (userService.existsByEmail(userCMD.getEmail())) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body("Email already exists");
        }
        User newUser = new User(
            userCMD.getUsername(),
            userCMD.getPassword(),
            userCMD.getEmail(),
            userCMD.getFirstname(),
            userCMD.getLastname(),
            userCMD.getAge(),
            userCMD.getCenter()
        );
        User registeredUser = userService.registerUser(newUser);
        return ResponseEntity.ok(registeredUser);
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) throws MessagingException {
        String email = request.get("email");
        LOG.info("Received email: {}", email);
        // Check if the email exists in the database
        Optional<User> optionalUser = userService.getUserByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("User with email " + email + " not found");
        }
        // Generate a unique reset token
        String resetToken = UUID.randomUUID().toString();
        User user = optionalUser.get();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userService.saveUser(user);
        LOG.info("Received email1: {}", resetToken);
        LOG.info("Received email2: {}", user.getFirstname());
        // Send reset password email with the token
        String domainUrl = "localhost:8080";
        String resetUrl = domainUrl + "/reset-password";
        LOG.info("Received email3: {}", resetUrl);
        emailService.sendResetInstructions(user.getEmail(), resetToken, resetUrl);
        return ResponseEntity.ok("Reset password email sent successfully");
    }
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String password = request.get("password");
        // Find the user by reset token
        Optional<User> optionalUser = userService.getUserByResetToken(token);
        if (optionalUser.isEmpty()) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Invalid or expired reset token");
        }
        // Update the user's password and reset token
        User user = optionalUser.get();
        user.setPassword(passwordEncoder.encode(password));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userService.saveUser(user);
        return ResponseEntity.ok("Password reset successful");
    }
}