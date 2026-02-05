package com.group.ticketmachine.auth

import java.security.MessageDigest
import java.sql.Connection

class AdminUserRepo(private val connection: Connection) {

    fun verify(username: String, password: String): Boolean {
        val sql = "SELECT password_hash FROM admin_users WHERE username = ?"
        connection.prepareStatement(sql).use { ps ->
            ps.setString(1, username.trim())
            ps.executeQuery().use { rs ->
                if (!rs.next()) return false
                val expectedHash = rs.getString("password_hash")
                return expectedHash.equals(sha256(password), ignoreCase = true)
            }
        }
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
