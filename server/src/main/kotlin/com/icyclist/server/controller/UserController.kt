package com.icyclist.server.controller

import com.icyclist.server.dto.LoginRequest
import com.icyclist.server.dto.LoginResponse
import com.icyclist.server.dto.ProfileRequest
import com.icyclist.server.dto.ProfileResponse
import com.icyclist.server.dto.UserRegistrationRequest
import com.icyclist.server.model.User
import com.icyclist.server.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @PostMapping("/register")
    fun register(@RequestBody registrationRequest: UserRegistrationRequest): ResponseEntity<*> {
        return try {
            val registeredUser = userService.register(registrationRequest)
            registeredUser.passwordHash = "" // Do not send hash back
            ResponseEntity.ok(registeredUser)
        } catch (e: RuntimeException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        }
    }

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<*> {
        return try {
            val (token, user) = userService.login(loginRequest)
            // 不返回密码哈希
            user.passwordHash = ""
            ResponseEntity.ok(LoginResponse(token, user))
        } catch (e: RuntimeException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.message)
        }
    }
    
    /**
     * 获取用户资料
     */
    @GetMapping("/profile/{userId}")
    fun getProfile(@PathVariable userId: Long): ResponseEntity<*> {
        return try {
            val user = userService.findById(userId)
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found")
            
            val profile = ProfileResponse(
                id = user.id ?: 0L,
                username = user.username,
                nickname = user.nickname,
                avatar = user.avatar,
                createdAt = user.createdAt?.toString()
            )
            ResponseEntity.ok(profile)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
        }
    }
    
    /**
     * 更新用户资料
     */
    @PutMapping("/profile/{userId}")
    fun updateProfile(
        @PathVariable userId: Long,
        @RequestBody profileRequest: ProfileRequest
    ): ResponseEntity<*> {
        return try {
            val updatedUser = userService.updateProfile(userId, profileRequest)
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found")
            
            // 不返回密码哈希
            updatedUser.passwordHash = ""
            
            val profile = ProfileResponse(
                id = updatedUser.id ?: 0L,
                username = updatedUser.username,
                nickname = updatedUser.nickname,
                avatar = updatedUser.avatar,
                createdAt = updatedUser.createdAt?.toString()
            )
            ResponseEntity.ok(profile)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
        }
    }
}
