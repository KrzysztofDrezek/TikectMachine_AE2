package com.group.ticketmachine.db.repo

import com.group.ticketmachine.TestDbFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CardRepoTest {

    private lateinit var dbAndPath: Pair<com.group.ticketmachine.db.Db, java.nio.file.Path>
    private val db get() = dbAndPath.first
    private val path get() = dbAndPath.second

    @BeforeEach
    fun setup() {
        dbAndPath = TestDbFactory.createTempDb("cards_test")
        TestDbFactory.createCardsTable(db)
        TestDbFactory.insertCard(db, "1111", 50.0)
        TestDbFactory.insertCard(db, "2222", 0.0)
    }

    @AfterEach
    fun tearDown() {
        TestDbFactory.dropTempDb(db, path)
    }

    @Test
    fun getCredit_returnsNull_whenCardMissing() {
        val repo = CardRepo(db.connection)
        val credit = repo.getCredit("9999")
        assertNull(credit)
    }

    @Test
    fun getCredit_returnsValue_whenCardExists() {
        val repo = CardRepo(db.connection)
        val credit = repo.getCredit("1111")
        assertNotNull(credit)
        assertEquals(50.0, credit!!, 0.0001)
    }

    @Test
    fun deduct_returnsFalse_whenCardNumberBlank() {
        val repo = CardRepo(db.connection)
        val ok = repo.deduct("   ", 10.0)
        assertFalse(ok)
    }

    @Test
    fun deduct_returnsFalse_whenAmountNotPositive() {
        val repo = CardRepo(db.connection)
        assertFalse(repo.deduct("1111", 0.0))
        assertFalse(repo.deduct("1111", -5.0))
    }

    @Test
    fun deduct_returnsFalse_whenInsufficientFunds() {
        val repo = CardRepo(db.connection)
        val ok = repo.deduct("2222", 1.0)
        assertFalse(ok)

        val after = repo.getCredit("2222")
        assertEquals(0.0, after!!, 0.0001)
    }

    @Test
    fun deduct_returnsTrue_andReducesCredit_whenEnoughFunds() {
        val repo = CardRepo(db.connection)

        val ok = repo.deduct("1111", 12.5)
        assertTrue(ok)

        val after = repo.getCredit("1111")
        assertEquals(37.5, after!!, 0.0001)
    }

    @Test
    fun deduct_trimsCardNumber() {
        val repo = CardRepo(db.connection)

        val ok = repo.deduct(" 1111 ", 10.0)
        assertTrue(ok)

        val after = repo.getCredit("1111")
        assertEquals(40.0, after!!, 0.0001)
    }
}
