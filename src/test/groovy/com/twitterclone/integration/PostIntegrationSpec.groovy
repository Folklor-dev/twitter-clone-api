package com.twitterclone.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.twitterclone.Application
import com.twitterclone.dto.LoginRequest
import com.twitterclone.dto.PostRequest
import com.twitterclone.models.Follow
import com.twitterclone.models.Post
import com.twitterclone.models.User
import com.twitterclone.repositories.CommentRepository
import com.twitterclone.repositories.FollowRepository
import com.twitterclone.repositories.LikeRepository
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Title("Post API Integration Tests")
@Narrative("Testing post creation, retrieval, updating, and deletion functionality")
@SpringBootTest(classes = Application)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Subject([Post])
class PostIntegrationSpec extends IntegrationTestBase {

    @Autowired
    MockMvc mockMvc

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    UserRepository userRepository

    @Autowired
    PostRepository postRepository

    @Autowired
    LikeRepository likeRepository

    @Autowired
    FollowRepository followRepository

    @Autowired
    CommentRepository commentRepository

    @Autowired
    PasswordEncoder passwordEncoder

    String authToken
    String userId
    User testUser

    def setup() {
        // Clean up before each test
        commentRepository.deleteAll()
        likeRepository.deleteAll()
        postRepository.deleteAll()
        userRepository.deleteAll()

        // Create a test user
        testUser = new User(
                username: "posttest",
                email: "post@test.com",
                password: passwordEncoder.encode("password123"),
                displayName: "Post Test User",
                bio: "Test bio"
        )
        testUser = userRepository.save(testUser)
        userId = testUser.id

        // Login to get auth token
        def loginRequest = new LoginRequest(
                username: "posttest",
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
        likeRepository.deleteAll()
        postRepository.deleteAll()
        userRepository.deleteAll()
    }

    def "should create a new post successfully"() {
        given: "a post request"
        def postRequest = new PostRequest(
                content: "This is a test post content"
        )

        when: "the create post endpoint is called"
        def result = mockMvc.perform(
                post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest))
                        .header("Authorization", "Bearer ${authToken}")
        )

        then: "the post is created successfully"
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.content').value(postRequest.content))
                .andExpect(jsonPath('$.userId').value(userId))
                .andExpect(jsonPath('$.username').value(testUser.username))
                .andExpect(jsonPath('$.displayName').value(testUser.displayName))
                .andExpect(jsonPath('$.likeCount').value(0))
                .andExpect(jsonPath('$.commentCount').value(0))
                .andExpect(jsonPath('$.liked').value(false))

        and: "the post exists in the database"
        def posts = postRepository.findByUserIdOrderByCreationDateDesc(userId, null)
        posts.content.size() == 1
        posts.content[0].content == postRequest.content
    }

    def "should get a post by ID"() {
        given: "an existing post"
        def post = new Post(userId: userId, content: "Existing post content")
        post = postRepository.save(post)

        when: "the get post endpoint is called"
        def result = mockMvc.perform(
                get("/api/posts/${post.id}")
                        .header("Authorization", "Bearer ${authToken}")
        )

        then: "the post is returned successfully"
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.id').value(post.id))
                .andExpect(jsonPath('$.content').value(post.content))
                .andExpect(jsonPath('$.userId').value(userId))
                .andExpect(jsonPath('$.username').value(testUser.username))
                .andExpect(jsonPath('$.displayName').value(testUser.displayName))
    }

    def "should update a post successfully"() {
        given: "an existing post"
        def post = new Post(userId: userId, content: "Original content")
        post = postRepository.save(post)

        and: "an update request"
        def updateRequest = new PostRequest(
                content: "Updated content"
        )

        when: "the update post endpoint is called"
        def result = mockMvc.perform(
                put("/api/posts/${post.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .header("Authorization", "Bearer ${authToken}")
        )

        then: "the post is updated successfully"
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.id').value(post.id))
                .andExpect(jsonPath('$.content').value(updateRequest.content))
                .andExpect(jsonPath('$.userId').value(userId))

        and: "the post is updated in the database"
        def updatedPost = postRepository.findById(post.id).orElse(null)
        updatedPost != null
        updatedPost.content == updateRequest.content
        updatedPost.lastEditDate != updatedPost.creationDate
    }

    def "should delete a post successfully"() {
        given: "an existing post"
        def post = new Post(userId: userId, content: "Post to be deleted")
        post = postRepository.save(post)

        when: "the delete post endpoint is called"
        def result = mockMvc.perform(
                delete("/api/posts/${post.id}")
                        .header("Authorization", "Bearer ${authToken}")
        )

        then: "the post is deleted successfully"
        result.andExpect(status().isOk())

        and: "the post no longer exists in the database"
        !postRepository.findById(post.id).isPresent()
    }

    def "should like and unlike a post"() {
        given: "an existing post"
        def postEntity = new Post(userId: userId, content: "Post to be liked")
        postEntity = postRepository.save(postEntity)

        when: "the like post endpoint is called"
        def likeResult = mockMvc.perform(
                post("/api/posts/${postEntity.id}/like")
                        .header("Authorization", "Bearer ${authToken}")
        )

        then: "the post is liked successfully"
        likeResult
                .andExpect(status().isOk())

        and: "the like exists in the database"
        likeRepository.existsByPostIdAndUserId(postEntity.id, userId)

        when: "the get post endpoint is called"
        def getResult = mockMvc.perform(
                get("/api/posts/${postEntity.id}")
                        .header("Authorization", "Bearer ${authToken}")
        )

        then: "the post response shows it's liked"
        getResult
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.likeCount').value(1))
                .andExpect(jsonPath('$.liked').value(true))

        when: "the unlike post endpoint is called"
        def unlikeResult = mockMvc.perform(
                delete("/api/posts/${postEntity.id}/like")
                        .header("Authorization", "Bearer ${authToken}")
        )

        then: "the post is unliked successfully"
        unlikeResult
                .andExpect(status().isOk())

        and: "the like no longer exists in the database"
        !likeRepository.existsByPostIdAndUserId(postEntity.id, userId)

        when: "the get post endpoint is called again"
        def getResultAfterUnlike = mockMvc.perform(
                get("/api/posts/${postEntity.id}")
                        .header("Authorization", "Bearer ${authToken}")
        )

        then: "the post response shows it's no longer liked"
        getResultAfterUnlike
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.likeCount').value(0))
                .andExpect(jsonPath('$.liked').value(false))
    }

    def "should get user posts"() {
        given: "multiple posts by the user"
        def post1 = new Post(userId: userId, content: "First post")
        def post2 = new Post(userId: userId, content: "Second post")
        def post3 = new Post(userId: userId, content: "Third post")
        postRepository.saveAll([post1, post2, post3])

        when: "the get user posts endpoint is called"
        def result = mockMvc.perform(
                get("/api/users/${userId}/posts")
                        .header("Authorization", "Bearer ${authToken}")
        )

        then: "the posts are returned successfully"
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.content.length()').value(3))
                .andExpect(jsonPath('$.content[0].userId').value(userId))
    }

    def "should get user feed"() {
        given: "multiple posts"

        def anotherUser = new User(
            username: "anotherfeeduser",
            email: "anotherfeed@test.com",
            password: passwordEncoder.encode("password123"),
            displayName: "Another Feed User"
        )
        anotherUser = userRepository.save(anotherUser)
        followRepository.save(new Follow(
            followerId: userId,
            followeeId: anotherUser.id
        ))

        def post1 = new Post(userId: userId, content: "Feed post 1")
        def post2 = new Post(userId: anotherUser.id, content: "Feed post 2")
        postRepository.saveAll([post1, post2])

        when: "the get feed endpoint is called"
        def result = mockMvc.perform(
                get("/api/feed")
                        .header("Authorization", "Bearer ${authToken}")
        )

        then: "the feed posts are returned successfully"
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.content.length()').value(2))
    }

    def "should not create post with invalid content"() {
        given: "a post request with empty content"
        def postRequest = new PostRequest(
                content: ""
        )

        when: "the create post endpoint is called"
        def result = mockMvc.perform(
                post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest))
                        .header("Authorization", "Bearer ${authToken}")
        )

        then: "the request fails with validation error"
        result.andExpect(status().isBadRequest())

        and: "no post is created in the database"
        postRepository.findByUserIdOrderByCreationDateDesc(userId, null).content.isEmpty()
    }

    def "should not update another user's post"() {
        given: "another user"
        def anotherUser = new User(
                username: "anotheruser",
                email: "another@test.com",
                password: passwordEncoder.encode("password123"),
                displayName: "Another User"
        )
        anotherUser = userRepository.save(anotherUser)

        and: "a post by another user"
        def post = new Post(userId: anotherUser.id, content: "Another user's post")
        post = postRepository.save(post)

        and: "an update request"
        def updateRequest = new PostRequest(
                content: "Trying to update another user's post"
        )

        when: "the update post endpoint is called"
        def result = mockMvc.perform(
                put("/api/posts/${post.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .header("Authorization", "Bearer ${authToken}")
        )

        then: "the request is forbidden"
        result.andExpect(status().isForbidden())

        and: "the post is not updated in the database"
        def unchangedPost = postRepository.findById(post.id).orElse(null)
        unchangedPost != null
        unchangedPost.content == post.content
    }

    def "should not delete another user's post"() {
        given: "another user"
        def anotherUser = new User(
                username: "anotherdeleteuser",
                email: "anotherdelete@test.com",
                password: passwordEncoder.encode("password123"),
                displayName: "Another Delete User"
        )
        anotherUser = userRepository.save(anotherUser)

        and: "a post by another user"
        def post = new Post(userId: anotherUser.id, content: "Another user's post to not delete")
        post = postRepository.save(post)

        when: "the delete post endpoint is called"
        def result = mockMvc.perform(
                delete("/api/posts/${post.id}")
                        .header("Authorization", "Bearer ${authToken}")
        )

        then: "the request is forbidden"
        result.andExpect(status().isForbidden())

        and: "the post still exists in the database"
        postRepository.findById(post.id).isPresent()
    }
}