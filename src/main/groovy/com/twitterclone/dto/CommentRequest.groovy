package com.twitterclone.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Comment creation request data")
class CommentRequest {
    @NotBlank(message = "Content is required")
    @Size(max = 280, message = "Content cannot exceed 280 characters")
    @Schema(description = "Content of the comment", example = "Great post! I totally agree with your points.", required = true)
    String content
}
