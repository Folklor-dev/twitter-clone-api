package com.twitterclone.repositories

import com.twitterclone.models.User
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUsername(String username)

    Optional<User> findByEmail(String email)

    boolean existsByUsername(String username)

    boolean existsByEmail(String email)
}
