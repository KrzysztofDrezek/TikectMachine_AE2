package com.group.ticketmachine.auth

class LoginService(private val store: List<User>) {
    var currentUser: User? = null
        private set

    fun login(username: String, password: String): Boolean {
        val user = store.firstOrNull { it.username == username && it.password == password }
        currentUser = user
        return user != null
    }

    fun isAdmin(): Boolean = currentUser?.isAdmin == true
    fun logout() { currentUser = null }
}
