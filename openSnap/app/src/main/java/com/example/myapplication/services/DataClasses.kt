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

data class Messages(
    val items: List<Message>,
    val page: Int,
    val perPage: Int,
    val totalItems: Int,
    val totalPages: Int
)

data class Message(
    val collectionId: String,
    val collectionName: String,
    val created: String,
    val id: String,
    val photo: String,
    val text: String,
    val updated: String,
    val user: String,
    val expand: Expand
)

data class Expand(
    val user: SearchableUser
)

data class SearchableUser(
    val avatar: String,
    val collectionId: String,
    val collectionName: String,
    val created: String,
    val emailVisibility: Boolean,
    val id: String,
    val name: String,
    val updated: String,
    val verified: Boolean
)