package com.twitterclone.models

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType

import java.time.LocalDateTime

@Document(collection = "users")
@Schema(description = "User entity")
class User {
    @Id
    @Schema(description = "Unique identifier", example = "60d21b4667d0d8992e610c86")
    @Field(targetType = FieldType.OBJECT_ID)
    String id

    @Indexed(unique = true)
    @Schema(description = "Unique username", example = "johndoe")
    String username

    @Indexed(unique = true)
    @Schema(description = "Email address", example = "john@example.com")
    String email

    @JsonIgnore
    @Schema(description = "Hashed password", hidden = true)
    String password

    @Schema(description = "Display name shown in UI", example = "John Doe")
    String displayName

    @Schema(description = "User bio/description", example = "Software developer and coffee enthusiast")
    String bio

    @Schema(description = "Date when user registered", example = "2023-01-01T10:00:00")
    LocalDateTime registrationDate = LocalDateTime.now()

}
