package com.twitterclone.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.twitterclone.Application
import com.twitterclone.dto.LoginRequest
import com.twitterclone.dto.UserUpdateRequest
import com.twitterclone.models.Follow
import com.twitterclone.models.User
import com.twitterclone.repositories.FollowRepository
import com.twitterclone.repositories.UserRepository
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import spock.lang.Narrative
import spock.lang.Subject
import spock.lang.Title

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Title("User API Integration Tests")
@Narrative("Testing user profile retrieval, update, and follow functionality")
@SpringBootTest(classes = Application)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Subject([User])
class UserIntegrationSpec extends IntegrationTestBase {

    @Autowired
    MockMvc mockMvc

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    UserRepository userRepository

    @Autowired
    FollowRepository followRepository

    @Autowired
    PasswordEncoder passwordEncoder

    String authToken
    String userId
    User testUser
    User otherUser

    def setup() {
        // Clean up before each test
        followRepository.deleteAll()
        userRepository.deleteAll()

        // Create test users
        testUser = new User(
                username: "usertest",
                email: "user@test.com",
                password: passwordEncoder.encode("password123"),
                displayName: "User Test",
                bio: "Test bio"
        )
        testUser = userRepository.save(testUser)
        userId = testUser.id

        otherUser = new User(
                username: "otheruser",
                email: "other@test.com",
                password: passwordEncoder.encode("password123"),
                displayName: "Other User",
                bio: "Other bio"
        )
        otherUser = userRepository.save(otherUser)

        // Login to get auth token
        def loginRequest = new LoginRequest(
                username: "usertest",
                password: "password123"
        )

        MvcResult loginResult = mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.token').exists())
                .andReturn()

        def responseBody = objectMapper.readValue(loginResult.response.contentAsString, Map)
        authToken = responseBody.token
    }

    def cleanup() {
        followRepository.deleteAll()
        userRepository.deleteAll()
    }

    def "should retrieve current user profile successfully"() {
        when: "the get current user endpoint is called"
        def result = mockMvc.perform(
                get("/api/users/me")
                        .header("Authorization", "Bearer ${authToken}")
        )

        then: "the user profile is retrieved successfully"
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.id').value(userId))
                .andExpect(jsonPath('$.username').value(testUser.username))
                .andExpect(jsonPath('$.displayName').value(testUser.displayName))
                .andExpect(jsonPath('$.bio').value(testUser.bio))
    }

    def "should retrieve user by id successfully"() {
        given: "a user id"
        String targetUserId = otherUser.id

        when: "the get user by id endpoint is called"
        def result = mockMvc.perform(
                get("/api/users/${targetUserId}")
                        .header("Authorization", "Bearer ${authToken}")
        )

        then: "the user profile is retrieved successfully"
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.id').value(targetUserId))
                .andExpect(jsonPath('$.username').value(otherUser.username))
                .andExpect(jsonPath('$.displayName').value(otherUser.displayName))
    }

    def "should retrieve user by username successfully"() {
        given: "a username"
        String targetUsername = otherUser.username

        when: "the get user by username endpoint is called"
        def result = mockMvc.perform(
                get("/api/users/username/${targetUsername}")
                        .header("Authorization", "Bearer ${authToken}")
        )

        then: "the user profile is retrieved successfully"
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.username').value(targetUsername))
                .andExpect(jsonPath('$.displayName').value(otherUser.displayName))
    }

    def "should update user profile successfully"() {
        given: "a user update request"
        def updateRequest = new UserUpdateRequest(
                displayName: "Updated Display Name",
                bio: "Updated bio information"
        )
        def testUserId = testUser.id
        when: "the update user endpoint is called"
        def result = mockMvc.perform(
                put("/api/users/${testUserId}")
                        .header("Authorization", "Bearer ${authToken}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
        )

        then: "the user is updated successfully"
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.displayName').value(updateRequest.displayName))
                .andExpect(jsonPath('$.bio').value(updateRequest.bio))

        and: "the user is updated in the database"
        def updatedUser = userRepository.findById(userId).orElse(null)
        updatedUser != null
        updatedUser.displayName == updateRequest.displayName
        updatedUser.bio == updateRequest.bio
    }

    def "should follow another user successfully"() {
        given: "another user"
        String targetUserId = otherUser.id

        when: "the follow user endpoint is called"
        def result = mockMvc.perform(
                post("/api/users/${targetUserId}/follow")
                        .header("Authorization", "Bearer ${authToken}")
        )

        then: "the follow is successful"
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.id').value(targetUserId))

        and: "the follow relationship is saved in the database"
        followRepository.existsByFollowerIdAndFolloweeId(userId, targetUserId)
    }

    def "should unfollow another user successfully"() {
        given: "a follow relationship"
        String targetUserId = otherUser.id
        followRepository.save(new Follow(followerId: userId, followeeId: targetUserId))

        when: "the unfollow user endpoint is called"
        def result = mockMvc.perform(
                delete("/api/users/${targetUserId}/follow")
                        .header("Authorization", "Bearer ${authToken}")
        )

        then: "the unfollow is successful"
        result
                .andExpect(status().isOk())

        and: "the follow relationship is removed from the database"
        !followRepository.existsByFollowerIdAndFolloweeId(userId, targetUserId)
    }

    def "should fail to follow yourself"() {
        when: "attempt to follow yourself"
        def result = mockMvc.perform(
                post("/api/users/${userId}/follow")
                        .header("Authorization", "Bearer ${authToken}")
        )

        then: "the request fails with bad request"
        result.andExpect(status().isBadRequest())
    }
    
    def "should return 404 when user not found"() {
        given: "a non-existent user id"
        String nonExistentId = new ObjectId().toHexString()
        
        when: "the get user endpoint is called"
        def result = mockMvc.perform(
                get("/api/users/${nonExistentId}")
                        .header("Authorization", "Bearer ${authToken}")
        )
        
        then: "the response is not found"
        result.andExpect(status().isNotFound())
    }

    def "should delete user successfully when authenticated as the same user"() {
        when: "the delete user endpoint is called"
        def result = mockMvc.perform(
                delete("/api/users/${userId}")
                        .header("Authorization", "Bearer ${authToken}")
        )

        then: "the user is deleted successfully"
        result.andExpect(status().isOk())

        and: "the user is no longer in the database"
        !userRepository.findById(userId).isPresent()
    }

    def "should return 403 when deleting another user without admin privileges"() {
        when: "trying to delete another user without admin privileges"
        def result = mockMvc.perform(
                delete("/api/users/${otherUser.id}")
                        .header("Authorization", "Bearer ${authToken}")
        )

        then: "access is denied"
        result.andExpect(status().isForbidden())

        and: "the other user still exists in the database"
        userRepository.findById(otherUser.id).isPresent()
    }

    def "should return 401 when deleting user without authentication"() {
        when: "trying to delete a user without authentication"
        def result = mockMvc.perform(
                delete("/api/users/${userId}")
        )

        then: "unauthorized response is returned"
        result.andExpect(status().isUnauthorized())

        and: "the user still exists in the database"
        userRepository.findById(userId).isPresent()
    }

}