package com.twitterclone.controllers

import com.twitterclone.dto.LoginRequest
import com.twitterclone.dto.UserRegistrationRequest
import com.twitterclone.dto.UserResponse
import com.twitterclone.models.User
import com.twitterclone.security.JwtTokenProvider
import com.twitterclone.services.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication operations")
class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager

    @Autowired
    private UserService userService

    @Autowired
    private JwtTokenProvider tokenProvider

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Login with username and password to get JWT token"
    )
    @ApiResponses(value = [
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            schema = @Schema(
                                    example = """
                        {
                          "token": "eyJhbGciOiJIUzUxMiJ9...",
                          "user": {
                            "id": "60d21b4667d0d8992e610c86",
                            "username": "testuser",
                            "displayName": "Test User",
                            "bio": "This is my bio",
                            "registrationDate": "2023-01-01T10:00:00",
                            "followeesCount": 5,
                            "followersCount": 10
                          }
                        }
                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Authentication failed")
    ])
    ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody
            @Parameter(
                    description = "Login credentials",
                    required = true,
                    schema = @Schema(
                            example = """
                    {
                      "username": "someuser",
                      "password": "password123"
                    }
                """
                    )
            )
                    LoginRequest loginRequest
    ) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.username,
                        loginRequest.password
                )
        )

        SecurityContextHolder.getContext().setAuthentication(authentication)

        String jwt = tokenProvider.generateToken(authentication)
        User user = userService.getUserByUsername(loginRequest.username)

        Map<String, Object> response = [
                token: jwt,
                user : userService.buildUserResponse(user)
        ]

        return ResponseEntity.ok(response)
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register user",
            description = "Register a new user account"
    )
    @ApiResponses(value = [
            @ApiResponse(
                    responseCode = "200",
                    description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Username or email already exists")
    ])
    ResponseEntity<UserResponse> register(
            @Valid @RequestBody
            @Parameter(
                    description = "User registration details",
                    required = true,
                    schema = @Schema(
                            example = """
                    {
                      "username": "someuser",
                      "email": "someuser@example.com",
                      "password": "password123",
                      "displayName": "Some User",
                      "bio": "Hello, I'm new here!"
                    }
                """
                    )
            )
                    UserRegistrationRequest registrationRequest
    ) {
        User user = userService.registerUser(registrationRequest)
        return ResponseEntity.ok(UserResponse.fromUser(user))
    }

    /**
     * User logout
     * Note: In a stateless JWT implementation, this is handled client-side
     * by removing the token. Server doesn't need to do anything.
     */
    @PostMapping("/logout")
    @Operation(
            summary = "User logout",
            description = "Logout the current user (client should discard JWT token)"
    )
    @ApiResponse(responseCode = "200", description = "Logout successful")
    ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok([message: "Logout successful"])
    }
}
