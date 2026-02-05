package com.group.ticketmachine.auth

import com.group.ticketmachine.TestDbFactory
import com.group.ticketmachine.db.Db
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.security.MessageDigest

class AdminUserRepoTest {

    private lateinit var dbAndPath: Pair<Db, Path>
    private val db get() = dbAndPath.first
    private val path get() = dbAndPath.second

    @BeforeEach
    fun setup() {
        dbAndPath = TestDbFactory.createTempDb("admin_users_test")
        TestDbFactory.createAdminUsersTable(db)

        val hash = sha256("admin123")
        TestDbFactory.insertAdminUser(db, "admin", hash)
    }

    @AfterEach
    fun tearDown() {
        TestDbFactory.dropTempDb(db, path)
    }

    @Test
    fun verify_returnsTrue_forCorrectPassword() {
        val repo = AdminUserRepo(db.connection)
        assertTrue(repo.verify("admin", "admin123"))
    }

    @Test
    fun verify_returnsFalse_forWrongPassword() {
        val repo = AdminUserRepo(db.connection)
        assertFalse(repo.verify("admin", "wrong"))
    }

    @Test
    fun verify_returnsFalse_forUnknownUser() {
        val repo = AdminUserRepo(db.connection)
        assertFalse(repo.verify("missing", "admin123"))
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
