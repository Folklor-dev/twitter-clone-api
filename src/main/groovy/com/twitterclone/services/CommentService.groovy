package com.twitterclone.services

import com.twitterclone.dto.CommentRequest
import com.twitterclone.dto.CommentResponse
import com.twitterclone.models.Comment
import com.twitterclone.models.User
import com.twitterclone.repositories.CommentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentService {

    @Autowired
    private CommentRepository commentRepository

    @Autowired
    private PostService postService

    @Autowired
    private UserService userService

    @Transactional
    Comment addComment(String postId, String userId, CommentRequest commentRequest) {
        // Check if post exists
        postService.getPostById(postId)

        Comment comment = new Comment(
                postId: postId,
                userId: userId,
                content: commentRequest.content
        )

        return commentRepository.save(comment)
    }

    Page<Comment> getCommentsForPost(String postId, Pageable pageable) {
        // Check if post exists
        postService.getPostById(postId)

        return commentRepository.findByPostIdOrderByCreationDateDesc(postId, pageable)
    }

    CommentResponse buildCommentResponse(Comment comment) {
        User author = userService.getUserById(comment.userId)

        return CommentResponse.fromComment(
                comment,
                author.username,
                author.displayName
        )
    }
}
