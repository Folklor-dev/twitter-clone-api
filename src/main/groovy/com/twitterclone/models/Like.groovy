package com.twitterclone.models


import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType

import java.time.LocalDateTime

/**
 * Like model representing a user's like on a post
 * Uses composite key of postId and userId to ensure uniqueness
 */
@Document(collection = "likes")
@CompoundIndexes([
        @CompoundIndex(name = "post_user_idx", def = "{'postId': 1, 'userId': 1}", unique = true)
])
class Like {
    @Id
    @Field(targetType = FieldType.OBJECT_ID)
    String id

    @Field(targetType = FieldType.OBJECT_ID)
    String postId

    @Field(targetType = FieldType.OBJECT_ID)
    String userId

    LocalDateTime createdAt = LocalDateTime.now()

    Like(String postId, String userId) {
        this.postId = postId
        this.userId = userId
    }

    /**
     * Default no-args constructor required by MongoDB
     */
    Like() {}
}