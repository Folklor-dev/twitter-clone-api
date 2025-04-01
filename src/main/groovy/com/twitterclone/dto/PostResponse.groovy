package com.twitterclone.dto


import io.swagger.v3.oas.annotations.media.Schema

import java.time.LocalDateTime

@Schema(description = "Post data returned by API")
class PostResponse {
    @Schema(description = "Unique identifier of the post", example = "60d21b4667d0d8992e610c85")
    String id

    @Schema(description = "ID of the user who created the post", example = "60d21b4667d0d8992e610c86")
    String userId

    @Schema(description = "Username of the post author", example = "johndoe")
    String username

    @Schema(description = "Display name of the post author", example = "John Doe")
    String displayName

    @Schema(description = "Content of the post", example = "This is an example post content!")
    String content

    @Schema(description = "Date when the post was created", example = "2023-01-15T14:30:00")
    LocalDateTime creationDate

    @Schema(description = "Date when the post was last edited", example = "2023-01-16T09:45:00")
    LocalDateTime lastEditDate

    @Schema(description = "Number of likes on the post", example = "42")
    int likeCount

    @Schema(description = "Number of comments on the post", example = "12")
    int commentCount

    @Schema(description = "Whether the current user has liked this post", example = "true")
    boolean liked

}
