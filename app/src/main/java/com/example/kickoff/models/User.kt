package com.example.kickoff.models

data class User(
    var uid: String = "",
    var username: String = "",
    var email: String = "",
    var password: String = "", // Added back for compatibility during migration
    var role: String = "user",
    var createdAt: Long = System.currentTimeMillis()
)