package com.twitterclone.dto

import com.twitterclone.models.User
import io.swagger.v3.oas.annotations.media.Schema

import java.time.LocalDateTime

@Schema(description = "User profile data returned by API")
class UserResponse {
    @Schema(description = "Unique identifier of the user", example = "60d21b4667d0d8992e610c86")
    String id

    @Schema(description = "Username", example = "johndoe")
    String username

    @Schema(description = "Display name shown in UI", example = "John Doe")
    String displayName

    @Schema(description = "User bio/description", example = "Software developer and coffee enthusiast")
    String bio

    @Schema(description = "Date when user registered", example = "2023-01-01T10:00:00")
    LocalDateTime registrationDate

    @Schema(description = "Number of users this user is following", example = "42")
    int followeesCount

    @Schema(description = "Number of users following this user", example = "128")
    int followersCount

    static UserResponse fromUser(User user) {
        return fromUser(user, 0, 0)
    }

    static UserResponse fromUser(User user, long followeesCount, long followersCount) {
        return new UserResponse(
                id: user.id,
                username: user.username,
                displayName: user.displayName,
                bio: user.bio,
                registrationDate: user.registrationDate,
                followeesCount: followeesCount,
                followersCount: followersCount
        )
    }
}
