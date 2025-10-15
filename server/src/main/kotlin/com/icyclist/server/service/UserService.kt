package com.icyclist.server.service

import com.icyclist.server.dto.LoginRequest
import com.icyclist.server.dto.UserRegistrationRequest
import com.icyclist.server.mapper.UserMapper
import com.icyclist.server.model.User
import com.icyclist.server.util.JwtTokenProvider
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userMapper: UserMapper,
    private val tokenProvider: JwtTokenProvider
) {

    private val passwordEncoder = BCryptPasswordEncoder()

    fun findByUsername(username: String): User? {
        return userMapper.findByUsername(username)
    }

    fun register(request: UserRegistrationRequest): User {
        if (findByUsername(request.username) != null) {
            throw RuntimeException("Username already exists")
        }

        val user = User(
            username = request.username,
            passwordHash = passwordEncoder.encode(request.password),
            nickname = request.nickname
        )

        userMapper.insert(user)
        return user
    }

    fun login(request: LoginRequest): String {
        val user = userMapper.findByUsername(request.username)
            ?: throw RuntimeException("User not found")

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw RuntimeException("Invalid password")
        }

        return tokenProvider.generateToken(user)
    }
}
