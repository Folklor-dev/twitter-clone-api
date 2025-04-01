package com.twitterclone.services

import com.twitterclone.models.Like
import com.twitterclone.repositories.LikeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LikeService {
    private final LikeRepository likeRepository

    @Autowired
    LikeService(LikeRepository likeRepository) {
        this.likeRepository = likeRepository
    }

    /**
     * Add a like to a post
     * @param postId the post ID
     * @param userId the user ID
     * @return true if like was added, false if it already exists
     */
    @Transactional
    boolean addLike(String postId, String userId) {
        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            return false
        }

        Like like = new Like(postId, userId)
        likeRepository.save(like)
        return true
    }

    /**
     * Remove a like from a post
     * @param postId the post ID
     * @param userId the user ID
     * @return true if like was removed, false if it didn't exist
     */
    @Transactional
    boolean removeLike(String postId, String userId) {
        Optional<Like> likeOptional = likeRepository.findByPostIdAndUserId(postId, userId)

        if (!likeOptional.isPresent()) {
            return false
        }

        likeRepository.deleteByPostIdAndUserId(postId, userId)
        return true
    }

    /**
     * Remove a likes from a post
     * @param postId the post ID
     * @return true if like was removed, false if it didn't exist
     */
    @Transactional
    boolean removeByPostId(String postId) {
        def count = likeRepository.countByPostId(postId)

        if (count == 0) {
            return false
        }

        likeRepository.deleteByPostId(postId)
        return true
    }

    /**
     * Check if a user has liked a post
     * @param postId the post ID
     * @param userId the user ID
     * @return true if the user has liked the post
     */
    boolean hasUserLikedPost(String postId, String userId) {
        return likeRepository.existsByPostIdAndUserId(postId, userId)
    }

    /**
     * Get the number of likes for a post
     * @param postId the post ID
     * @return the number of likes
     */
    long getLikeCount(String postId) {
        return likeRepository.countByPostId(postId)
    }

    /**
     * Get users who liked a post
     * @param postId the post ID
     * @return list of user IDs
     */
    List<String> getUsersWhoLikedPost(String postId) {
        return likeRepository.findByPostId(postId).collect { it.userId }
    }

    /**
     * Get posts liked by a user
     * @param userId the user ID
     * @return list of post IDs
     */
    List<String> getPostsLikedByUser(String userId) {
        return likeRepository.findByUserId(userId).collect { it.postId }
    }
}