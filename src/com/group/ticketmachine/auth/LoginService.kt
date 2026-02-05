package com.group.ticketmachine.auth

class LoginService(private val adminRepo: AdminUserRepo) {

    private var loggedInAdminUsername: String? = null

    fun authenticate(username: String, password: String): Boolean {
        val u = username.trim()
        if (u.isEmpty()) return false
        return adminRepo.verify(u, password)
    }

    fun isAdmin(username: String): Boolean {
        // Table contains only admins
        return username.trim().isNotEmpty()
    }

    fun login(username: String, password: String): Boolean {
        val u = username.trim()
        val ok = authenticate(u, password)
        loggedInAdminUsername = if (ok) u else null
        return ok
    }

    fun logout() {
        loggedInAdminUsername = null
    }

    fun isAdmin(): Boolean {
        return loggedInAdminUsername != null
    }

    fun currentUsername(): String? {
        return loggedInAdminUsername
    }
}
