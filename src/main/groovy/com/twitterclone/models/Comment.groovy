package com.twitterclone.models

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType

import java.time.LocalDateTime

@Document(collection = "comments")
@Schema(description = "Comment entity")
class Comment {
    @Id
    @Schema(description = "Unique identifier", example = "60d21b4667d0d8992e610c88")
    @Field(targetType = FieldType.OBJECT_ID)
    String id

    @Schema(description = "ID of the post this comment belongs to", example = "60d21b4667d0d8992e610c85")
    @Field(targetType = FieldType.OBJECT_ID)
    String postId

    @Schema(description = "ID of the user who created the comment", example = "60d21b4667d0d8992e610c86")
    @Field(targetType = FieldType.OBJECT_ID)
    String userId

    @Schema(description = "Content of the comment", example = "This is a great post!")
    String content

    @Schema(description = "Date when the comment was created", example = "2023-01-15T15:30:00")
    LocalDateTime creationDate = LocalDateTime.now()

}
