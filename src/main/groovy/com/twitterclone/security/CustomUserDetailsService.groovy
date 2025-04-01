package com.twitterclone.security

import com.twitterclone.exceptions.ResourceNotFoundException
import com.twitterclone.models.User
import com.twitterclone.repositories.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository

    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow { ->
                    new UsernameNotFoundException("User not found with username: $username")
                }

        return CustomUserDetails.build(user)
    }

    UserDetails loadUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow { ->
                    new ResourceNotFoundException("User not found with id: $id")
                }

        return CustomUserDetails.build(user)
    }
}
