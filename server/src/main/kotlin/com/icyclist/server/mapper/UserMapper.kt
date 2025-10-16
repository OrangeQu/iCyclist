package com.icyclist.server.mapper

import com.icyclist.server.model.User
import org.apache.ibatis.annotations.Mapper

@Mapper
interface UserMapper {
    fun findByUsername(username: String): User?
    fun insert(user: User)
}




















