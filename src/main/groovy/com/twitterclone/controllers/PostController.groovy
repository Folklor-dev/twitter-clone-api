package com.twitterclone.controllers

import com.twitterclone.dto.PostRequest
import com.twitterclone.dto.PostResponse
import com.twitterclone.models.Post
import com.twitterclone.security.CustomUserDetails
import com.twitterclone.services.PostService
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
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
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
@RequestMapping("/api")
@Tag(name = "Posts", description = "Post management operations")
class PostController {

    @Autowired
    private PostService postService

    @PostMapping("/posts")
    @Operation(
            summary = "Create post",
            description = "Create a new post with the provided content"
    )
    @ApiResponses(value = [
            @ApiResponse(
                    responseCode = "200",
                    description = "Post created successfully",
                    content = @Content(schema = @Schema(implementation = PostResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    ])
    ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody
            @Parameter(description = "Post content", required = true)
                    PostRequest postRequest,

            @AuthenticationPrincipal
                    CustomUserDetails currentUser
    ) {
        Post post = postService.createPost(currentUser.getId(), postRequest)
        PostResponse postResponse = postService.buildPostResponse(post, currentUser.getId())
        return ResponseEntity.ok(postResponse)
    }

    @GetMapping("/posts/{id}")
    @Operation(
            summary = "Get post",
            description = "Get a post by its ID",
            parameters = [
                    @Parameter(
                            name = "id",
                            description = "Post ID",
                            required = true,
                            in = ParameterIn.PATH,
                            schema = @Schema(type = "string", example = "60d21b4667d0d8992e610c85")
                    )
            ]
    )
    @ApiResponses(value = [
            @ApiResponse(
                    responseCode = "200",
                    description = "Found the post",
                    content = @Content(schema = @Schema(implementation = PostResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Post not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    ])
    ResponseEntity<PostResponse> getPost(
            @PathVariable(name = "id") String id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Post post = postService.getPostById(id)
        PostResponse postResponse = postService.buildPostResponse(post, currentUser.getId())
        return ResponseEntity.ok(postResponse)
    }

    @PutMapping("/posts/{id}")
    @Operation(
            summary = "Update post",
            description = "Update an existing post's content",
            parameters = [
                    @Parameter(
                            name = "id",
                            description = "Post ID",
                            required = true,
                            in = ParameterIn.PATH,
                            schema = @Schema(type = "string", example = "60d21b4667d0d8992e610c85")
                    )
            ]
    )
    @ApiResponses(value = [
            @ApiResponse(
                    responseCode = "200",
                    description = "Post updated successfully",
                    content = @Content(schema = @Schema(implementation = PostResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not the post owner"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    ])
    ResponseEntity<PostResponse> updatePost(
            @PathVariable(name = "id") String id,
            @Valid @RequestBody PostRequest postRequest,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Post post = postService.updatePost(id, currentUser.getId(), postRequest)
        PostResponse postResponse = postService.buildPostResponse(post, currentUser.getId())
        return ResponseEntity.ok(postResponse)
    }

    @DeleteMapping("/posts/{id}")
    @Operation(
            summary = "Delete post",
            description = "Delete an existing post",
            parameters = [
                    @Parameter(
                            name = "id",
                            description = "Post ID",
                            required = true,
                            in = ParameterIn.PATH,
                            schema = @Schema(type = "string", example = "60d21b4667d0d8992e610c85")
                    )
            ]
    )
    @ApiResponses(value = [
            @ApiResponse(responseCode = "200", description = "Post deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not the post owner"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    ])
    ResponseEntity<Map<String, String>> deletePost(
            @PathVariable(name = "id") String id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        postService.deletePost(id, currentUser.getId())
        return ResponseEntity.ok([message: "Post deleted successfully"])
    }

    @GetMapping("/users/{userId}/posts")
    @Operation(
            summary = "Get user posts",
            description = "Get all posts from a specific user with pagination",
            parameters = [
                    @Parameter(
                            name = "userId",
                            description = "User ID",
                            required = true,
                            in = ParameterIn.PATH,
                            schema = @Schema(type = "string", example = "60d21b4667d0d8992e610c86")
                    )
            ]
    )
    @ApiResponses(value = [
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user posts"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    ])
    ResponseEntity<Page<PostResponse>> getUserPosts(
            @PathVariable(name = "userId") String userId,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Page<Post> posts = postService.getPostsByUserId(userId, pageable)
        Page<PostResponse> postResponses = posts.map { post ->
            postService.buildPostResponse(post, currentUser.getId())
        }
        return ResponseEntity.ok(postResponses)
    }

    @GetMapping("/feed")
    @Operation(
            summary = "Get feed",
            description = "Get posts from followed users for the authenticated user with pagination"
    )
    @ApiResponses(value = [
            @ApiResponse(responseCode = "200", description = "Successfully retrieved feed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    ])
    ResponseEntity<Page<PostResponse>> getFeed(
            @PageableDefault(page = 0, size = 20, sort = "creationDate,desc") Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Page<Post> posts = postService.getFeedForUser(currentUser.getId(), pageable)
        Page<PostResponse> postResponses = posts.map { post ->
            postService.buildPostResponse(post, currentUser.getId())
        }
        return ResponseEntity.ok(postResponses)
    }

    @PostMapping("/posts/{id}/like")
    @Operation(
            summary = "Like post",
            description = "Like a post",
            parameters = [
                    @Parameter(
                            name = "id",
                            description = "Post ID",
                            required = true,
                            in = ParameterIn.PATH,
                            schema = @Schema(type = "string", example = "60d21b4667d0d8992e610c85")
                    )
            ]
    )
    @ApiResponses(value = [
            @ApiResponse(
                    responseCode = "200",
                    description = "Post liked successfully",
                    content = @Content(schema = @Schema(implementation = PostResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    ])
    ResponseEntity<PostResponse> likePost(
            @PathVariable(name = "id") String id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Post post = postService.likePost(id, currentUser.getId())
        PostResponse postResponse = postService.buildPostResponse(post, currentUser.getId())
        return ResponseEntity.ok(postResponse)
    }

    @DeleteMapping("/posts/{id}/like")
    @Operation(
            summary = "Unlike post",
            description = "Unlike a previously liked post",
            parameters = [
                    @Parameter(
                            name = "id",
                            description = "Post ID",
                            required = true,
                            in = ParameterIn.PATH,
                            schema = @Schema(type = "string", example = "60d21b4667d0d8992e610c85")
                    )
            ]
    )
    @ApiResponses(value = [
            @ApiResponse(
                    responseCode = "200",
                    description = "Post unliked successfully",
                    content = @Content(schema = @Schema(implementation = PostResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    ])
    ResponseEntity<PostResponse> unlikePost(
            @PathVariable(name = "id") String id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Post post = postService.unlikePost(id, currentUser.getId())
        PostResponse postResponse = postService.buildPostResponse(post, currentUser.getId())
        return ResponseEntity.ok(postResponse)
    }
}
