package com.twitterclone.repositories

import com.twitterclone.models.Follow
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface FollowRepository extends MongoRepository<Follow, String> {
    boolean existsByFollowerIdAndFolloweeId(String followerId, String followeeId)
    List<Follow> findByFollowerId(String followerId)
    List<Follow> findByFolloweeId(String followeeId)
    long countByFollowerId(String followerId)
    long countByFolloweeId(String followeeId)
    void deleteByFollowerIdAndFolloweeId(String followerId, String followeeId)
    void deleteByFollowerId(String followerId)
    void deleteByFolloweeId(String followeeId)
}
