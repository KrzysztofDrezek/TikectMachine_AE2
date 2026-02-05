package com.group.ticketmachine.auth

import com.group.ticketmachine.TestDbFactory
import com.group.ticketmachine.db.Db
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.security.MessageDigest

class LoginServiceTest {

    private lateinit var dbAndPath: Pair<Db, Path>
    private val db get() = dbAndPath.first
    private val path get() = dbAndPath.second

    @BeforeEach
    fun setup() {
        dbAndPath = TestDbFactory.createTempDb("login_service_test")
        TestDbFactory.createAdminUsersTable(db)

        val hash = sha256("admin123")
        TestDbFactory.insertAdminUser(db, "admin", hash)
    }

    @AfterEach
    fun tearDown() {
        TestDbFactory.dropTempDb(db, path)
    }

    @Test
    fun login_setsAdminSession_andLogoutClearsIt() {
        val adminRepo = AdminUserRepo(db.connection)
        val service = LoginService(adminRepo)

        assertFalse(service.isAdmin())
        assertNull(service.currentUsername())

        assertTrue(service.login("admin", "admin123"))
        assertTrue(service.isAdmin())
        assertEquals("admin", service.currentUsername())

        service.logout()
        assertFalse(service.isAdmin())
        assertNull(service.currentUsername())
    }

    @Test
    fun login_failsForWrongPassword() {
        val adminRepo = AdminUserRepo(db.connection)
        val service = LoginService(adminRepo)

        assertFalse(service.login("admin", "wrong"))
        assertFalse(service.isAdmin())
        assertNull(service.currentUsername())
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
