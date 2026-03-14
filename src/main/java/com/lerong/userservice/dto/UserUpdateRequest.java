package com.lerong.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * User Update Request DTO
 * Used for updating user information
 *
 * @author Claude Code
 * @version 1.0
 */
@Data
public class UserUpdateRequest {

    @Email(message = "Email must be valid")
    private String email;

    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    private String phone;

    private String role;

    private String avatar;

    private String bio;

    private Boolean enabled;

    private Boolean accountNonExpired;

    private Boolean accountNonLocked;

    private Boolean credentialsNonExpired;
}
