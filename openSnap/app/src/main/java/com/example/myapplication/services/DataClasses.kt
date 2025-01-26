package com.example.myapplication.services

data class NewUser(
    val email: String,
    val password: String,
    val passwordConfirm: String,
    val name: String
)

data class UserRecord(
    val avatar: String,
    val collectionId: String,
    val collectionName: String,
    val created: String,
    val email: String,
    val emailVisibility: Boolean,
    val id: String,
    val name: String,
    val updated: String,
    val verified: Boolean
)

data class AuthResponse(
    val record: UserRecord,
    val token: String
)