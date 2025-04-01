package com.twitterclone.dto

import com.twitterclone.models.Comment
import io.swagger.v3.oas.annotations.media.Schema

import java.time.LocalDateTime

@Schema(description = "Comment data returned by API")
class CommentResponse {
    @Schema(description = "Unique identifier of the comment", example = "60d21b4667d0d8992e610c88")
    String id

    @Schema(description = "ID of the user who created the comment", example = "60d21b4667d0d8992e610c86")
    String userId

    @Schema(description = "Username of the comment author", example = "johndoe")
    String username

    @Schema(description = "Display name of the comment author", example = "John Doe")
    String displayName

    @Schema(description = "Content of the comment", example = "This is a great post!")
    String content

    @Schema(description = "Date when the comment was created", example = "2023-01-15T15:30:00")
    LocalDateTime creationDate

    static CommentResponse fromComment(Comment comment, String username, String displayName) {
        return new CommentResponse(
                id: comment.id,
                userId: comment.userId,
                username: username,
                displayName: displayName,
                content: comment.content,
                creationDate: comment.creationDate
        )
    }
}
