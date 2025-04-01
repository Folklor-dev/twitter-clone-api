package com.twitterclone.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.twitterclone.Application
import com.twitterclone.dto.CommentRequest
import com.twitterclone.dto.LoginRequest
import com.twitterclone.models.Comment
import com.twitterclone.models.Post
import com.twitterclone.models.User
import com.twitterclone.repositories.CommentRepository
import com.twitterclone.repositories.PostRepository
import com.twitterclone.repositories.UserRepository
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Title("Comment API Integration Tests")
@Narrative("Testing comment creation and retrieval functionality")
@SpringBootTest(classes = Application)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Subject([Comment])
class CommentIntegrationSpec extends IntegrationTestBase {

    @Autowired
    MockMvc mockMvc

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    UserRepository userRepository

    @Autowired
    PostRepository postRepository

    @Autowired
    CommentRepository commentRepository

    @Autowired
    PasswordEncoder passwordEncoder

    String authToken
    String userId
    String postId
    User testUser
    Post testPost

    def setup() {
        // Clean up before each test
        commentRepository.deleteAll()
        postRepository.deleteAll()
        userRepository.deleteAll()

        // Create a test user
        testUser = new User(
            username: "commenttest",
            email: "comment@test.com",
            password: passwordEncoder.encode("password123"),
            displayName: "Comment Test User",
            bio: "Test bio"
        )
        testUser = userRepository.save(testUser)
        userId = testUser.id

        // Create a test post
        testPost = new Post(
            userId: userId,
            content: "This is a test post for comments"
        )
        testPost = postRepository.save(testPost)
        postId = testPost.id

        // Login to get auth token
        def loginRequest = new LoginRequest(
            username: "commenttest",
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
        commentRepository.deleteAll()
        postRepository.deleteAll()
        userRepository.deleteAll()
    }

    def "should add a comment to a post successfully"() {
        given: "a comment request"
        def commentRequest = new CommentRequest(
            content: "This is a test comment"
        )

        when: "the add comment endpoint is called"
        def result = mockMvc.perform(
            post("/api/posts/${postId}/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentRequest))
                .header("Authorization", "Bearer ${authToken}")
        )

        then: "the comment is added successfully"
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath('$.content').value(commentRequest.content))
            .andExpect(jsonPath('$.userId').value(userId))
            .andExpect(jsonPath('$.username').value(testUser.username))
            .andExpect(jsonPath('$.displayName').value(testUser.displayName))
            .andExpect(jsonPath('$.id').exists())
            .andExpect(jsonPath('$.creationDate').exists())

        and: "the comment exists in the database"
        def comments = commentRepository.findByPostIdOrderByCreationDateDesc(postId, null)
        comments.size() == 1
        comments[0].content == commentRequest.content
        comments[0].userId == userId
        comments[0].postId == postId
    }

    def "should return 400 when adding a comment with empty content"() {
        given: "a comment request with empty content"
        def commentRequest = new CommentRequest(
            content: ""
        )

        when: "the add comment endpoint is called"
        def result = mockMvc.perform(
            post("/api/posts/${postId}/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentRequest))
                .header("Authorization", "Bearer ${authToken}")
        )

        then: "the request fails with bad request"
        result.andExpect(status().isBadRequest())

        and: "no comment is added to the database"
        commentRepository.findByPostIdOrderByCreationDateDesc(postId, null).size() == 0
    }

    def "should return 401 when adding a comment without authentication"() {
        given: "a comment request"
        def commentRequest = new CommentRequest(
            content: "This is a test comment"
        )

        when: "the add comment endpoint is called without authentication"
        def result = mockMvc.perform(
            post("/api/posts/${postId}/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentRequest))
        )

        then: "the request fails with unauthorized"
        result.andExpect(status().isUnauthorized())

        and: "no comment is added to the database"
        commentRepository.findByPostIdOrderByCreationDateDesc(postId, null).size() == 0
    }

    def "should return 404 when adding a comment to a non-existent post"() {
        given: "a comment request and a non-existent post ID"
        def commentRequest = new CommentRequest(
            content: "This is a test comment"
        )
        def nonExistentPostId = "60d21b4667d0d8992e610c85"

        when: "the add comment endpoint is called with a non-existent post ID"
        def result = mockMvc.perform(
            post("/api/posts/${nonExistentPostId}/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentRequest))
                .header("Authorization", "Bearer ${authToken}")
        )

        then: "the request fails with not found"
        result.andExpect(status().isNotFound())
    }

    def "should get comments for a post with pagination"() {
        given: "multiple comments for a post"
        def commentCount = 5
        commentCount.times { i ->
            commentRepository.save(new Comment(
                postId: postId,
                userId: userId,
                content: "Test comment ${i + 1}"
            ))
        }

        when: "the get comments endpoint is called"
        def result = mockMvc.perform(
            get("/api/posts/${postId}/comments")
                .header("Authorization", "Bearer ${authToken}")
        )

        then: "the comments are retrieved successfully"
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath('$.content.length()').value(commentCount))
            .andExpect(jsonPath('$.totalElements').value(commentCount))

        and: "the comments contain the expected data"
        (0..<commentCount).each { i ->
            result
                .andExpect(jsonPath('$.content[' + i + '].userId').value(userId))
                .andExpect(jsonPath('$.content[' + i + '].username').value(testUser.username))
                .andExpect(jsonPath('$.content[' + i + '].displayName').value(testUser.displayName))
        }
    }

    def "should get empty comments list for a post with no comments"() {
        when: "the get comments endpoint is called for a post with no comments"
        def result = mockMvc.perform(
            get("/api/posts/${postId}/comments")
                .header("Authorization", "Bearer ${authToken}")
        )

        then: "an empty list is returned"
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath('$.content.length()').value(0))
            .andExpect(jsonPath('$.totalElements').value(0))
    }

    def "should return 404 when getting comments for a non-existent post"() {
        given: "a non-existent post ID"
        def nonExistentPostId = "60d21b4667d0d8992e610c85"

        when: "the get comments endpoint is called with a non-existent post ID"
        def result = mockMvc.perform(
            get("/api/posts/${nonExistentPostId}/comments")
                .header("Authorization", "Bearer ${authToken}")
        )

        then: "the request fails with not found"
        result.andExpect(status().isNotFound())
    }
}