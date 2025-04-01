package com.twitterclone.controllers

import com.twitterclone.dto.UserResponse
import com.twitterclone.dto.UserUpdateRequest
import com.twitterclone.models.User
import com.twitterclone.security.CustomUserDetails
import com.twitterclone.services.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management operations")
class UserController {

    @Autowired
    private UserService userService

    @GetMapping("/me")
    @Operation(
            summary = "Get current user",
            description = "Get the profile of the currently authenticated user"
    )
    @ApiResponses(value = [
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved user profile",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    ])
    ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userService.getUserById(currentUser.getId())
        return ResponseEntity.ok(userService.buildUserResponse(user))
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get user by ID",
            description = "Get a user's profile by their ID",
            parameters = [
                    @Parameter(
                            name = "id",
                            description = "User ID",
                            required = true,
                            in = ParameterIn.PATH,
                            schema = @Schema(type = "string", example = "60d21b4667d0d8992e610c86")
                    )
            ]
    )
    @ApiResponses(value = [
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved user profile",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    ])
    ResponseEntity<UserResponse> getUserById(
            @PathVariable(name = "id") String id
    ) {
        User user = userService.getUserById(id)
        return ResponseEntity.ok(userService.buildUserResponse(user))
    }

    @GetMapping("/username/{username}")
    @Operation(
            summary = "Get user by username",
            description = "Get a user's profile by their username",
            parameters = [
                    @Parameter(
                            name = "username",
                            description = "Username",
                            required = true,
                            in = ParameterIn.PATH,
                            schema = @Schema(type = "string", example = "testuser")
                    )
            ]
    )
    @ApiResponses(value = [
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved user profile",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    ])
    ResponseEntity<UserResponse> getUserByUsername(
            @PathVariable(name = "username") String username
    ) {
        User user = userService.getUserByUsername(username)
        return ResponseEntity.ok(userService.buildUserResponse(user))
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update user",
            description = "Update the current user's profile",
            parameters = [
                    @Parameter(
                            name = "id",
                            description = "User ID",
                            required = true,
                            in = ParameterIn.PATH,
                            schema = @Schema(type = "string", example = "60d21b4667d0d8992e610c86")
                    )
            ]
    )
    @ApiResponses(value = [
            @ApiResponse(
                    responseCode = "200",
                    description = "User profile updated successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not the profile owner"),
            @ApiResponse(responseCode = "404", description = "User not found")
    ])
    @PreAuthorize("authentication.principal.id == #id")
    ResponseEntity<UserResponse> updateUser(
            @PathVariable(name = "id") String id,

            @Valid @RequestBody
            @Parameter(
                    description = "Updated user profile data",
                    required = true,
                    schema = @Schema(
                            example = """
                    {
                      "displayName": "Updated Name",
                      "bio": "My new bio description"
                    }
                """
                    )
            )
                    UserUpdateRequest updateRequest
    ) {
        User updatedUser = userService.updateUser(id, updateRequest)
        return ResponseEntity.ok(userService.buildUserResponse(updatedUser))
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete user",
            description = "Delete the current user's account",
            parameters = [
                    @Parameter(
                            name = "id",
                            description = "User ID",
                            required = true,
                            in = ParameterIn.PATH,
                            schema = @Schema(type = "string", example = "60d21b4667d0d8992e610c86")
                    )
            ]
    )
    @ApiResponses(value = [
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not the account owner")
    ])
    @PreAuthorize("authentication.principal.id == #id")
    ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable(name = "id") String id
    ) {
        userService.deleteUser(id)
        return ResponseEntity.ok([message: "User deleted successfully"])
    }

    @PostMapping("/{id}/follow")
    @Operation(
            summary = "Follow user",
            description = "Follow another user",
            parameters = [
                    @Parameter(
                            name = "id",
                            description = "User ID to follow",
                            required = true,
                            in = ParameterIn.PATH,
                            schema = @Schema(type = "string", example = "60d21b4667d0d8992e610c87")
                    )
            ]
    )
    @ApiResponses(value = [
            @ApiResponse(
                    responseCode = "200",
                    description = "User followed successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Cannot follow yourself"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    ])
    ResponseEntity<UserResponse> followUser(
            @PathVariable(name = "id") String id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        UserResponse userResponse = userService.followUser(currentUser.getId(), id)
        return ResponseEntity.ok(userResponse)
    }

    @DeleteMapping("/{id}/follow")
    @Operation(
            summary = "Unfollow user",
            description = "Unfollow a previously followed user",
            parameters = [
                    @Parameter(
                            name = "id",
                            description = "User ID to unfollow",
                            required = true,
                            in = ParameterIn.PATH,
                            schema = @Schema(type = "string", example = "60d21b4667d0d8992e610c87")
                    )
            ]
    )
    @ApiResponses(value = [
            @ApiResponse(
                    responseCode = "200",
                    description = "User unfollowed successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    ])
    ResponseEntity<UserResponse> unfollowUser(
            @PathVariable(name = "id") String id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        UserResponse userResponse = userService.unfollowUser(currentUser.getId(), id)
        return ResponseEntity.ok(userResponse)
    }
}
