package com.twitterclone.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "User registration request data")
class UserRegistrationRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @Schema(description = "Unique username", example = "johndoe", required = true)
    String username

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(description = "Email address", example = "john@example.com", required = true)
    String email

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Schema(description = "User password (min 6 characters)", example = "securePassword123", required = true)
    String password

    @NotBlank(message = "Display name is required")
    @Size(max = 50, message = "Display name cannot exceed 50 characters")
    @Schema(description = "Display name shown in UI", example = "John Doe", required = true)
    String displayName

    @Size(max = 160, message = "Bio cannot exceed 160 characters")
    @Schema(description = "User bio/description", example = "Software developer and coffee enthusiast")
    String bio
}
