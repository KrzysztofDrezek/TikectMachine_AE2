package com.group.ticketmachine.auth

class LoginService(
    private val userStore: UserStore = UserStore.default()
) {
    var currentUser: User? = null
        private set

    fun login(username: String, password: String): Boolean {
        val u = username.trim()
        val ok = userStore.validate(u, password)
        currentUser = if (ok) userStore.findUser(u) else null
        return ok
    }

    fun logout() {
        currentUser = null
    }

    fun isAdmin(): Boolean {
        val u = currentUser?.username ?: return false
        return userStore.isAdmin(u)
    }
}
