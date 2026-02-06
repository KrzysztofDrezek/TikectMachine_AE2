package com.group.ticketmachine.db.repo

import com.group.ticketmachine.TestDbFactory
import com.group.ticketmachine.db.Db
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Path

class CardRepoTest {

    private lateinit var dbAndPath: Pair<Db, Path>
    private val db get() = dbAndPath.first
    private val path get() = dbAndPath.second

    private val card1 = "1111111111111111"
    private val card2 = "2222222222222222"

    @BeforeEach
    fun setup() {
        dbAndPath = TestDbFactory.createTempDb("cards_test")
        TestDbFactory.createCardsTable(db)
        TestDbFactory.insertCard(db, card1, 50.0)
        TestDbFactory.insertCard(db, card2, 0.0)
    }

    @AfterEach
    fun tearDown() {
        TestDbFactory.dropTempDb(db, path)
    }

    @Test
    fun getCredit_returnsNull_whenCardMissing() {
        val repo = CardRepo(db.connection)
        val credit = repo.getCredit("9999999999999999")
        assertNull(credit)
    }

    @Test
    fun getCredit_returnsValue_whenCardExists() {
        val repo = CardRepo(db.connection)
        val credit = repo.getCredit(card1)
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
        assertFalse(repo.deduct(card1, 0.0))
        assertFalse(repo.deduct(card1, -5.0))
    }

    @Test
    fun deduct_returnsFalse_whenInsufficientFunds() {
        val repo = CardRepo(db.connection)

        val ok = repo.deduct(card2, 1.0)
        assertFalse(ok)

        val after = repo.getCredit(card2)
        assertNotNull(after)
        assertEquals(0.0, after!!, 0.0001)
    }

    @Test
    fun deduct_returnsTrue_andReducesCredit_whenEnoughFunds() {
        val repo = CardRepo(db.connection)

        val ok = repo.deduct(card1, 12.5)
        assertTrue(ok)

        val after = repo.getCredit(card1)
        assertNotNull(after)
        assertEquals(37.5, after!!, 0.0001)
    }

    @Test
    fun deduct_trimsCardNumber() {
        val repo = CardRepo(db.connection)

        val ok = repo.deduct("  $card1  ", 10.0)
        assertTrue(ok)

        val after = repo.getCredit(card1)
        assertNotNull(after)
        assertEquals(40.0, after!!, 0.0001)
    }
}
