package com.twitterclone.services

import com.twitterclone.dto.PostRequest
import com.twitterclone.dto.PostResponse
import com.twitterclone.exceptions.ForbiddenException
import com.twitterclone.exceptions.ResourceNotFoundException
import com.twitterclone.models.Post
import com.twitterclone.repositories.CommentRepository
import com.twitterclone.repositories.FollowRepository
import com.twitterclone.repositories.PostRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PostService {

    @Autowired
    private PostRepository postRepository

    @Autowired
    private CommentRepository commentRepository

    @Autowired
    private FollowRepository followRepository

    @Autowired
    private UserService userService

    @Autowired
    private LikeService likeService

    @Transactional
    Post createPost(String userId, PostRequest postRequest) {
        // Check if user exists
        userService.getUserById(userId)

        Post post = new Post(
                userId: userId,
                content: postRequest.content
        )

        return postRepository.save(post)
    }

    Post getPostById(String id) {
        try {
            return postRepository.findById(id)
                    .orElseThrow({ -> new ResourceNotFoundException("Post not found with id: $id") })
        } catch (Exception e) {
            if (e instanceof ResourceNotFoundException) {
                throw e
            } else {
                throw new ResourceNotFoundException("Post not found with id: $id")
            }
        }
    }

    @Transactional
    Post updatePost(String id, String userId, PostRequest postRequest) {
        Post post = getPostById(id)

        // Check if user is the author of the post
        if (post.userId != userId) {
            throw new ForbiddenException("You don't have permission to update this post")
        }

        post.updateContent(postRequest.content)

        return postRepository.save(post)
    }

    @Transactional
    void deletePost(String id, String userId) {
        Post post = getPostById(id)

        // Check if user is the author of the post
        if (post.userId != userId) {
            throw new ForbiddenException("You don't have permission to delete this post")
        }

        likeService.removeByPostId(id)
        commentRepository.deleteByPostId(id)
        postRepository.delete(post)
    }

    Page<Post> getPostsByUserId(String userId, Pageable pageable) {
        // Check if user exists
        userService.getUserById(userId)

        return postRepository.findByUserIdOrderByCreationDateDesc(userId, pageable)
    }

    Page<Post> getFeedForUser(String userId, Pageable pageable) {
        // Check if user exists
        userService.getUserById(userId)

        return findFeedPostsForUser(userId, pageable)
    }

    Page<Post> findFeedPostsForUser(String userId, Pageable pageable) {
        // Check if user exists
        userService.getUserById(userId)

        def followeeIds = followRepository.findByFollowerId(userId).collect {it.followeeId }
        followeeIds.add(userId)
        return postRepository.findByUserIdInOrderByCreationDateDesc(followeeIds, pageable)
    }

    @Transactional
    Post likePost(String id, String userId) {
        // First check if the post exists
        Post post = getPostById(id)
        likeService.addLike(id, userId)
        return post
    }

    @Transactional
    Post unlikePost(String id, String userId) {
        // First check if the post exists
        Post post = getPostById(id)
        likeService.removeLike(id, userId)
        return post
    }

    PostResponse buildPostResponse(Post post, String currentUserId) {
        def user = userService.getUserById(post.userId)
        def likesCount = likeService.getLikeCount(post.id)
        def hasLiked = likeService.hasUserLikedPost(post.id, currentUserId)
        def commentCount = commentRepository.countByPostId(post.id)

        return new PostResponse(
                id: post.id,
                content: post.content,
                userId: post.userId,
                username: user.username,
                displayName: user.displayName,
                creationDate: post.creationDate,
                lastEditDate: post.lastEditDate,
                likeCount: likesCount,
                commentCount: commentCount,
                liked: hasLiked
        )
    }

}
