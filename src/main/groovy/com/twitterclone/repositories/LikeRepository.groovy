package com.twitterclone.repositories

import com.twitterclone.models.Like
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository


@Repository
interface LikeRepository extends MongoRepository<Like, String> {

    Optional<Like> findByPostIdAndUserId(String postId, String userId)

    boolean existsByPostIdAndUserId(String postId, String userId)

    long countByPostId(String postId)

    List<Like> findByPostId(String postId)

    List<Like> findByUserId(String userId)

    void deleteByPostIdAndUserId(String postId, String userId)

    void deleteByPostId(String postId)
    void deleteByUserId(String userId)
}