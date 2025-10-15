package com.icyclist.server.controller

import com.icyclist.server.dto.LoginRequest
import com.icyclist.server.dto.LoginResponse
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
            val token = userService.login(loginRequest)
            ResponseEntity.ok(LoginResponse(token))
        } catch (e: RuntimeException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.message)
        }
    }
}
