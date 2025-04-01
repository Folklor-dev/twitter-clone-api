package com.twitterclone.controllers

import com.twitterclone.dto.CommentRequest
import com.twitterclone.dto.CommentResponse
import com.twitterclone.models.Comment
import com.twitterclone.security.CustomUserDetails
import com.twitterclone.services.CommentService
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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/posts")
@Tag(name = "Comments", description = "Comment management operations")
class CommentController {

    @Autowired
    private CommentService commentService

    @PostMapping("/{postId}/comments")
    @Operation(
            summary = "Add comment",
            description = "Add a comment to a post",
            parameters = [
                    @Parameter(
                            name = "postId",
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
                    description = "Comment added successfully",
                    content = @Content(schema = @Schema(implementation = CommentResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    ])
    ResponseEntity<CommentResponse> addComment(
            @PathVariable(name = "postId") String postId,

            @Valid @RequestBody
            @Parameter(description = "Comment content", required = true)
                    CommentRequest commentRequest,

            @AuthenticationPrincipal
                    CustomUserDetails currentUser
    ) {
        Comment comment = commentService.addComment(postId, currentUser.getId(), commentRequest)
        CommentResponse commentResponse = commentService.buildCommentResponse(comment)
        return ResponseEntity.ok(commentResponse)
    }

    @GetMapping("/{postId}/comments")
    @Operation(
            summary = "Get comments",
            description = "Get all comments for a post with pagination",
            parameters = [
                    @Parameter(
                            name = "postId",
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
                    description = "Successfully retrieved comments"
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    ])
    ResponseEntity<Page<CommentResponse>> getCommentsForPost(
            @PathVariable(name = "postId") String postId,

            @PageableDefault(size = 20)
            @Parameter(description = "Pagination parameters (page, size, sort)")
                    Pageable pageable
    ) {
        Page<Comment> comments = commentService.getCommentsForPost(postId, pageable)
        Page<CommentResponse> commentResponses = comments.map { comment ->
            commentService.buildCommentResponse(comment)
        }
        return ResponseEntity.ok(commentResponses)
    }
}
