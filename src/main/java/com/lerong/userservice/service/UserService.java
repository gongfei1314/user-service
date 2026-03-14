package com.lerong.userservice.service;

import com.lerong.userservice.entity.User;
import com.lerong.userservice.exception.ResourceNotFoundException;
import com.lerong.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * User Service
 * Business logic for user management
 *
 * @author Claude Code
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Create a new user
     */
    public User createUser(User user) {
        log.info("Creating new user: {}", user.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Set default values
        if (user.getEnabled() == null) {
            user.setEnabled(true);
        }
        if (user.getRole() == null) {
            user.setRole("USER");
        }
        if (user.getAccountNonExpired() == null) {
            user.setAccountNonExpired(true);
        }
        if (user.getAccountNonLocked() == null) {
            user.setAccountNonLocked(true);
        }
        if (user.getCredentialsNonExpired() == null) {
            user.setCredentialsNonExpired(true);
        }

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        log.info("Fetching user by ID: {}", id);
        return userRepository.findById(id);
    }

    /**
     * Get user by username
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        log.info("Fetching user by username: {}", username);
        return userRepository.findByUsername(username);
    }

    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        log.info("Fetching user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    /**
     * Get all users with pagination
     */
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        log.info("Fetching all users with pagination");
        return userRepository.findAll(pageable);
    }

    /**
     * Get all users without pagination
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll();
    }

    /**
     * Get users by role
     */
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(String role) {
        log.info("Fetching users by role: {}", role);
        return userRepository.findByRole(role);
    }

    /**
     * Get enabled users
     */
    @Transactional(readOnly = true)
    public List<User> getEnabledUsers() {
        log.info("Fetching enabled users");
        return userRepository.findByEnabledTrue();
    }

    /**
     * Search users by keyword
     */
    @Transactional(readOnly = true)
    public List<User> searchUsers(String keyword) {
        log.info("Searching users with keyword: {}", keyword);
        return userRepository.searchUsers(keyword);
    }

    /**
     * Update user
     */
    public User updateUser(Long id, User userDetails) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        // Update fields if provided
        if (userDetails.getEmail() != null && !userDetails.getEmail().equals(user.getEmail())) {
            // Check if new email already exists
            if (userRepository.existsByEmail(userDetails.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + userDetails.getEmail());
            }
            user.setEmail(userDetails.getEmail());
        }

        if (userDetails.getFullName() != null) {
            user.setFullName(userDetails.getFullName());
        }

        if (userDetails.getPhone() != null) {
            user.setPhone(userDetails.getPhone());
        }

        if (userDetails.getRole() != null) {
            user.setRole(userDetails.getRole());
        }

        if (userDetails.getAvatar() != null) {
            user.setAvatar(userDetails.getAvatar());
        }

        if (userDetails.getBio() != null) {
            user.setBio(userDetails.getBio());
        }

        if (userDetails.getEnabled() != null) {
            user.setEnabled(userDetails.getEnabled());
        }

        if (userDetails.getAccountNonExpired() != null) {
            user.setAccountNonExpired(userDetails.getAccountNonExpired());
        }

        if (userDetails.getAccountNonLocked() != null) {
            user.setAccountNonLocked(userDetails.getAccountNonLocked());
        }

        if (userDetails.getCredentialsNonExpired() != null) {
            user.setCredentialsNonExpired(userDetails.getCredentialsNonExpired());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", updatedUser.getId());
        return updatedUser;
    }

    /**
     * Update user password
     */
    public void updatePassword(Long id, String oldPassword, String newPassword) {
        log.info("Updating password for user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        // Encode and set new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password updated successfully for user ID: {}", id);
    }

    /**
     * Reset user password (admin operation)
     */
    public void resetPassword(Long id, String newPassword) {
        log.info("Resetting password for user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password reset successfully for user ID: {}", id);
    }

    /**
     * Update last login time
     */
    public void updateLastLogin(Long id) {
        log.info("Updating last login time for user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Last login time updated for user ID: {}", id);
    }

    /**
     * Delete user
     */
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully: {}", id);
    }

    /**
     * Enable/disable user
     */
    public void toggleUserEnabled(Long id, Boolean enabled) {
        log.info("{} user with ID: {}", enabled ? "Enabling" : "Disabling", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        user.setEnabled(enabled);
        userRepository.save(user);

        log.info("User {} successfully: {}", id, enabled ? "enabled" : "disabled");
    }

    /**
     * Count users by role
     */
    @Transactional(readOnly = true)
    public long countByRole(String role) {
        return userRepository.countByRole(role);
    }

    /**
     * Check if username exists
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Check if email exists
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
