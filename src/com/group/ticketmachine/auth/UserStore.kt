package com.group.ticketmachine.auth

class UserStore(private val users: List<User>) {

    fun validate(username: String, password: String): Boolean {
        return users.any { it.username == username && it.password == password }
    }

    fun isAdmin(username: String): Boolean {
        return users.firstOrNull { it.username == username }?.isAdmin == true
    }

    fun findUser(username: String): User? {
        return users.firstOrNull { it.username == username }
    }

    companion object {
        fun default(): UserStore {
            return UserStore(
                listOf(
                    User(username = "admin", password = "admin123", isAdmin = true),
                    User(username = "user", password = "user123", isAdmin = false)
                )
            )
        }
    }
}
