package com.twitterclone.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.twitterclone.dto.PostRequest
import com.twitterclone.dto.PostResponse
import com.twitterclone.models.Post
import com.twitterclone.services.PostService
import org.bson.types.ObjectId
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class PostControllerSpec extends Specification {

    PostController postController
    PostService postService
    MockMvc mockMvc
    ObjectMapper objectMapper
    SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor requestPostProcessor

    def setup() {
        postService = Mock(PostService)
        postController = new PostController(postService: postService)
        mockMvc = MockMvcBuilders.standaloneSetup(postController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build()
        objectMapper = new ObjectMapper()
        requestPostProcessor = SecurityMockMvcRequestPostProcessors
            .user("testuser")
            .roles("USER")
            .password("password")
    }

    def "should create a post"() {
        given:
        def userId = new ObjectId().toHexString()
        def postRequest = new PostRequest(content: "This is a new post")
        def postEntity = new Post(id: new ObjectId().toString(), userId: userId, content: postRequest.content)
        def postResponse = new PostResponse(
            id: postEntity.id,
            userId: postEntity.userId,
            username: "testuser",
            displayName: "Test User",
            content: "This is a new post",
            likeCount: 0,
            commentCount: 0,
            liked: false
        )

        when:
        def result = mockMvc.perform(
            post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postRequest))
                .with(requestPostProcessor)
        )

        then:
        1 * postService.createPost(_, { it.content == postRequest.content }) >> postEntity
        1 * postService.buildPostResponse(postEntity, _) >> postResponse

        and:
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath('$.id').value(postResponse.id))
            .andExpect(jsonPath('$.username').value(postResponse.username))
            .andExpect(jsonPath('$.content').value(postResponse.content))
    }

    def "should get a post by ID"() {
        given:
        def userId = new ObjectId().toString()
        def postId = new ObjectId().toString()

        def postEntity = new Post(id: postId, userId: userId, content: "Test post content")
        def postResponse = new PostResponse(
            id: postId,
            userId: userId,
            username: "testuser",
            displayName: "Test User",
            content: "Test post content",
            likeCount: 5,
            commentCount: 2,
            liked: true
        )

        when:
        def result = mockMvc.perform(
            get("/api/posts/${postId}")
                .with(requestPostProcessor)
        )

        then:
        1 * postService.getPostById(postId) >> postEntity
        1 * postService.buildPostResponse(postEntity, _) >> postResponse

        and:
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath('$.id').value(postResponse.id))
            .andExpect(jsonPath('$.content').value(postResponse.content))
            .andExpect(jsonPath('$.likeCount').value(postResponse.likeCount))
    }

    def "should update a post"() {
        given:
        def userId = new ObjectId().toString()
        def postId = new ObjectId().toString()

        def updateRequest = new PostRequest(content: "Updated post content")
        def updatedPost = new Post(id: postId, userId: userId, content: updateRequest.content)
        def updatedResponse = new PostResponse(
            id: postId,
            userId: userId,
            username: "testuser",
            displayName: "Test User",
            content: "Updated post content",
            likeCount: 0,
            commentCount: 0,
            liked: false
        )

        when:
        def result = mockMvc.perform(
            put("/api/posts/${postId}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .with(requestPostProcessor)
        )

        then:
        1 * postService.updatePost(postId, _, { it.content == updateRequest.content }) >> updatedPost
        1 * postService.buildPostResponse(updatedPost, _) >> updatedResponse

        and:
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath('$.id').value(updatedResponse.id))
            .andExpect(jsonPath('$.content').value(updatedResponse.content))
    }

    def "should delete a post"() {
        given:
        def postId = new ObjectId().toString()

        when:
        def result = mockMvc.perform(
            delete("/api/posts/${postId}")
                .with(requestPostProcessor)
        )

        then:
        1 * postService.deletePost(postId, _)

        and:
        result.andExpect(status().isOk())
    }

    def "should get a user's post feed"() {
        given:
        def userId = new ObjectId().toString()
        def postId = new ObjectId().toString()
        def postEntity = new Post(id: postId, userId: userId, content: "Test post content")
        def postResponse = new PostResponse(
            id: postId,
            userId: userId,
            username: "testuser",
            displayName: "Test User",
            content: "Test post content",
            likeCount: 5,
            commentCount: 2,
            liked: true
        )

        def pageable = Pageable.ofSize(10).withPage(0)
        def postPage = new PageImpl<>([postEntity], pageable, 1)

        when:
        def result = mockMvc.perform(
            get("/api/feed")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "creationDate,desc")
                .with(requestPostProcessor)
        ).andDo(MockMvcResultHandlers.print())

        then:
        1 * postService.getFeedForUser(_, _) >> postPage
        1 * postService.buildPostResponse(postEntity, _) >> postResponse

        and:
        result.andExpect(status().isOk())
    }
}