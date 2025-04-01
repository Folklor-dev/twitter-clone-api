package com.twitterclone.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Login request data")
class LoginRequest {
    @NotBlank(message = "Username is required")
    @Schema(description = "Username for login", example = "johndoe", required = true)
    String username

    @NotBlank(message = "Password is required")
    @Schema(description = "User password", example = "password123", required = true)
    String password
}
