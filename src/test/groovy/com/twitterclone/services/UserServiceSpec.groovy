package com.twitterclone.services

import com.twitterclone.dto.UserRegistrationRequest
import com.twitterclone.dto.UserUpdateRequest
import com.twitterclone.exceptions.BadRequestException
import com.twitterclone.exceptions.ResourceNotFoundException
import com.twitterclone.models.User
import com.twitterclone.repositories.FollowRepository
import com.twitterclone.repositories.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import spock.lang.Specification

class UserServiceSpec extends Specification {

    UserService userService
    UserRepository userRepository
    FollowRepository followRepository
    PasswordEncoder passwordEncoder

    def setup() {
        userRepository = Mock(UserRepository)
        followRepository = Mock(FollowRepository)
        passwordEncoder = Mock(PasswordEncoder)
        userService = new UserService(
            userRepository: userRepository,
            followRepository: followRepository,
            passwordEncoder: passwordEncoder
        )
    }

    def "should register a new user successfully"() {
        given:
        def request = new UserRegistrationRequest(
            username: "testuser",
            email: "test@example.com",
            password: "password123",
            displayName: "Test User",
            bio: "This is my bio"
        )

        def savedUser = new User(
            id: "1",
            username: request.username,
            email: request.email,
            password: "encodedPassword",
            displayName: request.displayName,
            bio: request.bio
        )

        when:
        def result = userService.registerUser(request)

        then:
        1 * userRepository.existsByUsername(request.username) >> false
        1 * userRepository.existsByEmail(request.email) >> false
        1 * passwordEncoder.encode(request.password) >> "encodedPassword"
        1 * userRepository.save(_) >> savedUser

        and:
        result.id == "1"
        result.username == request.username
        result.email == request.email
        result.displayName == request.displayName
        result.bio == request.bio
    }

    def "should throw BadRequestException when username already exists"() {
        given:
        def request = new UserRegistrationRequest(
            username: "existinguser",
            email: "test@example.com",
            password: "password123",
            displayName: "Test User"
        )

        when:
        userService.registerUser(request)

        then:
        1 * userRepository.existsByUsername(request.username) >> true
        thrown(BadRequestException)
    }

    def "should throw BadRequestException when email already exists"() {
        given:
        def request = new UserRegistrationRequest(
            username: "testuser",
            email: "existing@example.com",
            password: "password123",
            displayName: "Test User"
        )

        when:
        userService.registerUser(request)

        then:
        1 * userRepository.existsByUsername(request.username) >> false
        1 * userRepository.existsByEmail(request.email) >> true
        thrown(BadRequestException)
    }

    def "should get user by ID"() {
        given:
        def userId = "1"
        def user = new User(
            id: userId,
            username: "testuser",
            email: "test@example.com",
            displayName: "Test User"
        )

        when:
        def result = userService.getUserById(userId)

        then:
        1 * userRepository.findById(userId) >> Optional.of(user)

        and:
        result.id == userId
        result.username == "testuser"
        result.email == "test@example.com"
    }

    def "should throw ResourceNotFoundException when user ID doesn't exist"() {
        given:
        def userId = "nonexistentId"

        when:
        userService.getUserById(userId)

        then:
        1 * userRepository.findById(userId) >> Optional.empty()
        thrown(ResourceNotFoundException)
    }

    def "should update user profile"() {
        given:
        def userId = "1"
        def user = new User(
            id: userId,
            username: "testuser",
            email: "test@example.com",
            displayName: "Old Name",
            bio: "Old bio"
        )

        def updateRequest = new UserUpdateRequest(
            displayName: "New Name",
            bio: "New bio"
        )

        def updatedUser = new User(
            id: userId,
            username: "testuser",
            email: "test@example.com",
            displayName: updateRequest.displayName,
            bio: updateRequest.bio
        )

        when:
        def result = userService.updateUser(userId, updateRequest)

        then:
        1 * userRepository.findById(userId) >> Optional.of(user)
        1 * userRepository.save(_) >> updatedUser

        and:
        result.displayName == "New Name"
        result.bio == "New bio"
    }

    def "should follow a user"() {
        given:
        def currentUserId = "1"
        def targetUserId = "2"

        def currentUser = new User(
            id: currentUserId,
            username: "currentuser",
            email: "current@example.com",
            displayName: "Current User"
        )

        def targetUser = new User(
            id: targetUserId,
            username: "targetuser",
            email: "target@example.com",
            displayName: "Target User"
        )

        when:
        userService.followUser(currentUserId, targetUserId)

        then:
        1 * userRepository.findById(currentUserId) >> Optional.of(currentUser)
        1 * userRepository.findById(targetUserId) >> Optional.of(targetUser)
        1 * followRepository.existsByFollowerIdAndFolloweeId(currentUserId, targetUserId)
        1 * followRepository.save({ it.followerId == currentUserId && it.followeeId == targetUserId })
    }

    def "should throw BadRequestException when trying to follow self"() {
        given:
        def userId = "1"

        when:
        userService.followUser(userId, userId)

        then:
        thrown(BadRequestException)
        0 * userRepository.save(_)
    }
}
