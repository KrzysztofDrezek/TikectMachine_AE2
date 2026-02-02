package com.group.ticketmachine.auth

object UserStore {
    // Hard-coded users
    val users = listOf(
        User(username = "admin", password = "admin123", isAdmin = true),
        User(username = "user",  password = "user123",  isAdmin = false)
    )
}
