package cn.edu.zjgsu.mysoftapplication3

data class User(
    val id: String,
    val name: String,
    val avatar: String,
    val bio: String,
    val followers: Int = 0,
    val following: Int = 0
)