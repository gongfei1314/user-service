package com.lerong.userservice.controller;

import com.lerong.userservice.dto.UserCreateRequest;
import com.lerong.userservice.dto.UserResponse;
import com.lerong.userservice.dto.UserUpdateRequest;
import com.lerong.userservice.entity.User;
import com.lerong.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User Controller
 * Full CRUD user management APIs
 *
 * @author Claude Code
 * @version 1.0
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "user-service");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new user
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("Creating user: {}", request.getUsername());

        User user = convertToEntity(request);
        User createdUser = userService.createUser(user);

        return new ResponseEntity<>(convertToResponse(createdUser), HttpStatus.CREATED);
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.info("Fetching user by ID: {}", id);

        User user = userService.getUserById(id)
                .orElseThrow(() -> new com.lerong.userservice.exception.ResourceNotFoundException("User", id));

        return ResponseEntity.ok(convertToResponse(user));
    }

    /**
     * Get user by username
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        log.info("Fetching user by username: {}", username);

        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new com.lerong.userservice.exception.ResourceNotFoundException("User", "username", username));

        return ResponseEntity.ok(convertToResponse(user));
    }

    /**
     * Get all users with pagination
     */
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("Fetching all users with pagination - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> users = userService.getAllUsers(pageable);
        Page<UserResponse> responses = users.map(this::convertToResponse);

        return ResponseEntity.ok(responses);
    }

    /**
     * Get all users without pagination
     */
    @GetMapping("/all")
    public ResponseEntity<List<UserResponse>> getAllUsersList() {
        log.info("Fetching all users");

        List<User> users = userService.getAllUsers();
        List<UserResponse> responses = users.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Get users by role
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable String role) {
        log.info("Fetching users by role: {}", role);

        List<User> users = userService.getUsersByRole(role);
        List<UserResponse> responses = users.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Search users by keyword
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String keyword) {
        log.info("Searching users with keyword: {}", keyword);

        List<User> users = userService.searchUsers(keyword);
        List<UserResponse> responses = users.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Update user
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {

        log.info("Updating user with ID: {}", id);

        User userDetails = convertToEntity(request);
        User updatedUser = userService.updateUser(id, userDetails);

        return ResponseEntity.ok(convertToResponse(updatedUser));
    }

    /**
     * Delete user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with ID: {}", id);

        userService.deleteUser(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User deleted successfully");
        response.put("deletedUserId", id.toString());

        return ResponseEntity.ok(response);
    }

    /**
     * Enable/disable user
     */
    @PatchMapping("/{id}/enabled")
    public ResponseEntity<UserResponse> toggleUserEnabled(
            @PathVariable Long id,
            @RequestParam Boolean enabled) {

        log.info("{} user with ID: {}", enabled ? "Enabling" : "Disabling", id);

        userService.toggleUserEnabled(id, enabled);

        User user = userService.getUserById(id)
                .orElseThrow(() -> new com.lerong.userservice.exception.ResourceNotFoundException("User", id));

        return ResponseEntity.ok(convertToResponse(user));
    }

    /**
     * Reset user password (admin operation)
     */
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @PathVariable Long id,
            @RequestParam String newPassword) {

        log.info("Resetting password for user ID: {}", id);

        userService.resetPassword(id, newPassword);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset successfully");
        response.put("userId", id.toString());

        return ResponseEntity.ok(response);
    }

    /**
     * Get statistics
     */
    @GetMapping("/stats/summary")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("Fetching user statistics");

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userService.getAllUsers().size());
        stats.put("enabledUsers", userService.getEnabledUsers().size());
        stats.put("adminCount", userService.countByRole("ADMIN"));
        stats.put("userCount", userService.countByRole("USER"));

        return ResponseEntity.ok(stats);
    }

    // Helper methods to convert between Entity and DTO

    private UserResponse convertToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getRole(),
                user.getEnabled(),
                user.getAvatar(),
                user.getBio(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLoginAt()
        );
    }

    private User convertToEntity(UserCreateRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setAvatar(request.getAvatar());
        user.setBio(request.getBio());
        return user;
    }

    private User convertToEntity(UserUpdateRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setAvatar(request.getAvatar());
        user.setBio(request.getBio());
        user.setEnabled(request.getEnabled());
        user.setAccountNonExpired(request.getAccountNonExpired());
        user.setAccountNonLocked(request.getAccountNonLocked());
        user.setCredentialsNonExpired(request.getCredentialsNonExpired());
        return user;
    }
}
