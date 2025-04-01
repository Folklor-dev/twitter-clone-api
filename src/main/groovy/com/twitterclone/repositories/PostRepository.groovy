package com.twitterclone.repositories

import com.twitterclone.models.Post
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface PostRepository extends MongoRepository<Post, String> {
    Page<Post> findByUserIdOrderByCreationDateDesc(String userId, Pageable pageable)
    Page<Post> findByUserIdInOrderByCreationDateDesc(List<String> userIds, Pageable pageable)
}
