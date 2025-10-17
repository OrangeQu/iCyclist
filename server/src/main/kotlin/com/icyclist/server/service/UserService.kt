package com.icyclist.server.service

import com.icyclist.server.dto.LoginRequest
import com.icyclist.server.dto.ProfileRequest
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

    fun login(request: LoginRequest): Pair<String, User> {
        val user = userMapper.findByUsername(request.username)
            ?: throw RuntimeException("User not found")

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw RuntimeException("Invalid password")
        }

        val token = tokenProvider.generateToken(user)
        return Pair(token, user)
    }
    
    /**
     * 根据ID查找用户
     */
    fun findById(userId: Long): User? {
        return userMapper.findById(userId)
    }
    
    /**
     * 更新用户资料
     */
    fun updateProfile(userId: Long, profileRequest: ProfileRequest): User? {
        val user = userMapper.findById(userId) ?: return null
        
        // 更新昵称
        profileRequest.nickname?.let {
            user.nickname = it
        }
        
        // 更新头像
        profileRequest.avatar?.let {
            user.avatar = it
        }
        
        userMapper.updateProfile(user)
        return user
    }
}
