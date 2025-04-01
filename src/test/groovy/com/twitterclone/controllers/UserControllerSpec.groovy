package com.twitterclone.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.twitterclone.dto.UserResponse
import com.twitterclone.dto.UserUpdateRequest
import com.twitterclone.models.User
import com.twitterclone.services.UserService
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class UserControllerSpec extends Specification {

    UserController userController
    UserService userService
    MockMvc mockMvc
    ObjectMapper objectMapper

    def setup() {
        userService = Mock(UserService)
        userController = new UserController(userService: userService)
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build()
        objectMapper = new ObjectMapper()
    }

    def "should get current user"() {
        given:
        def userId = "1"

        def user = new User(
                id: userId,
                username: "testuser",
                email: "test@example.com",
                displayName: "Test User",
                bio: "This is my bio"
        )

        def userResponse = UserResponse.fromUser(user)

        when:
        def result = mockMvc.perform(
                get("/api/users/me")
                        .with(SecurityMockMvcRequestPostProcessors.user("testuser").roles("USER").password("password"))
        )

        then:
        1 * userService.getUserById(_) >> user
        1 * userService.buildUserResponse(user) >> userResponse

        and:
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.id').value(userResponse.id))
                .andExpect(jsonPath('$.username').value(userResponse.username))
                .andExpect(jsonPath('$.displayName').value(userResponse.displayName))
    }

    def "should get user by ID"() {
        given:
        def userId = "1"
        def user = new User(
                id: userId,
                username: "testuser",
                email: "test@example.com",
                displayName: "Test User",
                bio: "This is my bio"
        )

        def userResponse = UserResponse.fromUser(user)

        when:
        def result = mockMvc.perform(
                get("/api/users/${userId}")
                        .with(SecurityMockMvcRequestPostProcessors.user("testuser").roles("USER"))
        )

        then:
        1 * userService.getUserById(userId) >> user
        1 * userService.buildUserResponse(user) >> userResponse

        and:
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.id').value(userResponse.id))
                .andExpect(jsonPath('$.username').value(userResponse.username))
                .andExpect(jsonPath('$.displayName').value(userResponse.displayName))
    }

    def "should update user"() {
        given:
        def userId = "1"
        def updateRequest = new UserUpdateRequest(
                displayName: "New Name",
                bio: "New bio"
        )

        def updatedUser = new User(
                id: userId,
                username: "testuser",
                email: "test@example.com",
                displayName: updateRequest.displayName,
                bio: updateRequest.bio
        )

        def userResponse = UserResponse.fromUser(updatedUser)

        when:
        def result = mockMvc.perform(
                put("/api/users/${userId}")
                        .with(SecurityMockMvcRequestPostProcessors.user("testuser").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
        )

        then:
        1 * userService.updateUser(userId, _) >> updatedUser
        1 * userService.buildUserResponse(updatedUser) >> userResponse

        and:
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.displayName').value(updateRequest.displayName))
                .andExpect(jsonPath('$.bio').value(updateRequest.bio))
    }

    def "should follow a user"() {
        given:
        def targetUserId = "2"
        def targetUser = new User(
                id: targetUserId,
                username: "targetuser",
                displayName: "Target User",
                bio: "Target bio"
        )

        def userResponse = UserResponse.fromUser(targetUser)

        when:
        def result = mockMvc.perform(
                post("/api/users/${targetUserId}/follow")
                        .with(SecurityMockMvcRequestPostProcessors.user("testuser").roles("USER"))
        )

        then:
        1 * userService.followUser(_, targetUserId) >> userResponse

        and:
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.id').value(userResponse.id))
                .andExpect(jsonPath('$.username').value(userResponse.username))
                .andExpect(jsonPath('$.followersCount').value(userResponse.followersCount))
    }

    def "should unfollow a user"() {
        given:
        def targetUserId = "2"
        def targetUser = new User(
                id: targetUserId,
                username: "targetuser",
                displayName: "Target User",
                bio: "Target bio"
        )

        def userResponse = UserResponse.fromUser(targetUser)

        when:
        def result = mockMvc.perform(
                delete("/api/users/${targetUserId}/follow")
                        .with(SecurityMockMvcRequestPostProcessors.user("testuser").roles("USER"))
        )

        then:
        1 * userService.unfollowUser(_, targetUserId) >> userResponse

        and:
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.id').value(userResponse.id))
                .andExpect(jsonPath('$.username').value(userResponse.username))
                .andExpect(jsonPath('$.followersCount').value(userResponse.followersCount))
    }
}
