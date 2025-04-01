package com.twitterclone.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size

@Schema(description = "User profile update request data")
class UserUpdateRequest {
    @Size(max = 50, message = "Display name cannot exceed 50 characters")
    @Schema(description = "Updated display name", example = "John Smith")
    String displayName

    @Size(max = 160, message = "Bio cannot exceed 160 characters")
    @Schema(description = "Updated user bio/description", example = "Software architect and tech enthusiast")
    String bio
}
