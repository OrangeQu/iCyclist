package com.icyclist.server.mapper

import com.icyclist.server.model.User
import org.apache.ibatis.annotations.Mapper

@Mapper
interface UserMapper {
    fun findByUsername(username: String): User?
    fun findById(id: Long): User?
    fun insert(user: User)
    fun updateProfile(user: User)
}






















