package com.twitterclone.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Post creation or update request data")
class PostRequest {
    @NotBlank(message = "Content is required")
    @Size(max = 280, message = "Content cannot exceed 280 characters")
    @Schema(description = "Content of the post", example = "This is my new post about Spring Boot and MongoDB!", required = true)
    String content
}
