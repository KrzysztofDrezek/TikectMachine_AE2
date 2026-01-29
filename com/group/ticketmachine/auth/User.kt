package com.group.ticketmachine.auth

data class User(
    val username: String,
    val password: String,
    val isAdmin: Boolean
)
