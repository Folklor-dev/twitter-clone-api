package com.twitterclone.models

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType

import java.time.LocalDateTime

@Document(collection = "posts")
@Schema(description = "Post entity")
class Post {
    @Id
    @Schema(description = "Unique identifier", example = "60d21b4667d0d8992e610c85")
    @Field(targetType = FieldType.OBJECT_ID)
    String id

    @Schema(description = "ID of the user who created the post", example = "60d21b4667d0d8992e610c86")
    @Field(targetType = FieldType.OBJECT_ID)
    String userId

    @Schema(description = "Content of the post", example = "This is an example post content!")
    String content

    @Schema(description = "Date when the post was created", example = "2023-01-15T14:30:00")
    LocalDateTime creationDate = LocalDateTime.now()

    @Schema(description = "Date when the post was last edited", example = "2023-01-16T09:45:00")
    LocalDateTime lastEditDate = LocalDateTime.now()

    void updateContent(String newContent) {
        this.content = newContent
        this.lastEditDate = LocalDateTime.now()
    }
}
