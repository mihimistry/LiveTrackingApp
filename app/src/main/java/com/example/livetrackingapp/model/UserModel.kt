package com.example.livetrackingapp.model

class UserModel(
    val userName: String? = null,
    val email: String? = null,
    val password: String? = null
) {
    enum class UserEnum {
        USER, userName, email, password
    }
}