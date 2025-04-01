package com.twitterclone.models


import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType

import java.time.LocalDateTime

@Document(collection = "follows")
@CompoundIndex(name = "follower_following_idx", def = "{'followerId': 1, 'followingId': 1}", unique = true)
class Follow {
    @Id
    @Field(targetType = FieldType.OBJECT_ID)
    String id

    @Field(targetType = FieldType.OBJECT_ID)
    String followerId
    @Field(targetType = FieldType.OBJECT_ID)
    String followeeId

    LocalDateTime createdAt = LocalDateTime.now()
}
