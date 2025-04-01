package com.twitterclone.repositories

import com.twitterclone.models.Comment
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface CommentRepository extends MongoRepository<Comment, String> {

    Page<Comment> findByPostIdOrderByCreationDateDesc(String postId, Pageable pageable)

    long countByPostId(String postId)

    void deleteByPostId(String postId)
}
