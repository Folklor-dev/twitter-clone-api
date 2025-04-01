package com.twitterclone.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.twitterclone.Application
import com.twitterclone.dto.LoginRequest
import com.twitterclone.dto.UserRegistrationRequest
import com.twitterclone.models.User
import com.twitterclone.repositories.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Narrative
import spock.lang.Subject
import spock.lang.Title

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Title("User Registration and Login Tests")
@Narrative("Testing user registration and login functionality")
@SpringBootTest(classes = Application)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Subject([User])
class AuthIntegrationSpec extends IntegrationTestBase {

    @Autowired
    MockMvc mockMvc

    @Autowired
    UserRepository userRepository

    @Autowired
    PasswordEncoder passwordEncoder

    @Autowired
    ObjectMapper objectMapper

    def cleanup() {
        userRepository.deleteAll()
    }

    def "should register a new user successfully"() {
        given: "a user registration request"
        def registrationRequest = new UserRegistrationRequest(
                username: "spocktest",
                email: "spock@test.com",
                password: "password123",
                displayName: "Spock Test",
                bio: "Spock test bio"
        )

        when: "the registration endpoint is called"
        def registrationResult = mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest))
        )

        then: "the user is registered successfully"
        registrationResult
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.username').value(registrationRequest.username))
                .andExpect(jsonPath('$.displayName').value(registrationRequest.displayName))

        and: "the user exists in the database"
        def savedUser = userRepository.findByUsername(registrationRequest.username).orElse(null)
        savedUser != null
        savedUser.username == registrationRequest.username
        savedUser.email == registrationRequest.email
        passwordEncoder.matches(registrationRequest.password, savedUser.password)
    }

    def "should login successfully with valid credentials"() {
        given: "an existing user"
        def username = "logintest"
        def password = "password123"
        userRepository.save(new User(
                username: username,
                email: "login@test.com",
                password: passwordEncoder.encode(password),
                displayName: "Login Test"
        ))

        and: "a login request"
        def loginRequest = new LoginRequest(
                username: username,
                password: password
        )

        when: "the login endpoint is called"
        def result = mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
        )

        then: "the login is successful"
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.token').exists())
                .andExpect(jsonPath('$.user.username').value(username))
    }

    def "should not register user with duplicate username"() {
        given: "an existing user"
        userRepository.save(new User(
                username: "existinguser",
                email: "existing@test.com",
                password: passwordEncoder.encode("password123"),
                displayName: "Existing User"
        ))

        and: "a registration request with the same username"
        def registrationRequest = new UserRegistrationRequest(
                username: "existinguser",
                email: "new@test.com",
                password: "password123",
                displayName: "New User"
        )

        when: "the registration endpoint is called"
        def result = mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest))
        )

        then: "the registration fails with a bad request status"
        result.andExpect(status().isBadRequest())
    }

    def "should not login with invalid credentials"() {
        given: "an existing user"
        userRepository.save(new User(
                username: "testuser",
                email: "test@test.com",
                password: passwordEncoder.encode("correctpassword"),
                displayName: "Test User"
        ))

        and: "a login request with incorrect password"
        def loginRequest = new LoginRequest(
                username: "testuser",
                password: "wrongpassword"
        )

        when: "the login endpoint is called"
        def result = mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
        )

        then: "the login fails with an unauthorized status"
        result.andExpect(status().isUnauthorized())
    }
}
