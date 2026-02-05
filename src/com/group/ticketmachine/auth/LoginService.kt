package com.group.ticketmachine.auth

class LoginService(private val userStore: UserStore = UserStore.default()) {

    var currentUser: User? = null
        private set

    fun authenticate(username: String, password: String): Boolean {
        return userStore.validate(username.trim(), password)
    }

    fun isAdmin(username: String): Boolean {
        return userStore.isAdmin(username.trim())
    }

    fun login(username: String, password: String): Boolean {
        val u = username.trim()
        val ok = authenticate(u, password)
        currentUser = if (ok) userStore.find(u) else null
        return ok
    }

    fun logout() {
        currentUser = null
    }

    fun isAdmin(): Boolean {
        return currentUser?.isAdmin == true
    }
}
