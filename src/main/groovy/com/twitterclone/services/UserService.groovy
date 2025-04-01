package com.twitterclone.services

import com.twitterclone.dto.UserRegistrationRequest
import com.twitterclone.dto.UserResponse
import com.twitterclone.dto.UserUpdateRequest
import com.twitterclone.exceptions.BadRequestException
import com.twitterclone.exceptions.ResourceNotFoundException
import com.twitterclone.models.Follow
import com.twitterclone.models.User
import com.twitterclone.repositories.FollowRepository
import com.twitterclone.repositories.LikeRepository
import com.twitterclone.repositories.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService {

    @Autowired
    private UserRepository userRepository

    @Autowired
    private FollowRepository followRepository

    @Autowired
    private LikeRepository likeRepository

    @Autowired
    private PasswordEncoder passwordEncoder

    @Transactional
    User registerUser(UserRegistrationRequest registrationRequest) {
        // Check if username is already taken
        if (userRepository.existsByUsername(registrationRequest.username)) {
            throw new BadRequestException("Username is already taken")
        }

        // Check if email is already taken
        if (userRepository.existsByEmail(registrationRequest.email)) {
            throw new BadRequestException("Email is already in use")
        }

        User user = new User(
                username: registrationRequest.username,
                email: registrationRequest.email,
                password: passwordEncoder.encode(registrationRequest.password),
                displayName: registrationRequest.displayName,
                bio: registrationRequest.bio
        )

        return userRepository.save(user)
    }

    User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow({ -> new ResourceNotFoundException("User not found with id: $id") })
    }

    User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow({ -> new ResourceNotFoundException("User not found with username: $username") })
    }

    @Transactional
    User updateUser(String id, UserUpdateRequest updateRequest) {
        User user = getUserById(id)

        if (updateRequest.displayName) {
            user.displayName = updateRequest.displayName
        }

        if (updateRequest.bio != null) {
            user.bio = updateRequest.bio
        }

        return userRepository.save(user)
    }

    @Transactional
    void deleteUser(String id) {
        User user = getUserById(id)
        likeRepository.deleteByUserId(user.id)
        followRepository.deleteByFollowerId(user.id)
        followRepository.deleteByFolloweeId(user.id)
        userRepository.delete(user)
    }

    @Transactional
    UserResponse followUser(String currentUserId, String targetUserId) {
        if (currentUserId == targetUserId) {
            throw new BadRequestException("You cannot follow yourself")
        }

        getUserById(currentUserId)
        User targetUser = getUserById(targetUserId)

        if (!followRepository.existsByFollowerIdAndFolloweeId(currentUserId, targetUserId)) {
            Follow follow = new Follow(
                    followerId: currentUserId,
                followeeId: targetUserId
            )
            followRepository.save(follow)
        }
        return buildUserResponse(targetUser)
    }

    @Transactional
    UserResponse unfollowUser(String currentUserId, String targetUserId) {
        getUserById(currentUserId)
        User targetUser = getUserById(targetUserId)
        followRepository.deleteByFollowerIdAndFolloweeId(currentUserId, targetUserId)
        return buildUserResponse(targetUser)
    }

    UserResponse buildUserResponse(User user) {
        return UserResponse.fromUser(user,
                followRepository.countByFollowerId(user.id),
                followRepository.countByFolloweeId(user.id)
        )
    }
}
