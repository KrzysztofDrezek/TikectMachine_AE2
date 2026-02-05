package com.group.ticketmachine.db.repo

import com.group.ticketmachine.TestDbFactory
import com.group.ticketmachine.db.Db
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Path

class TicketRepoTest {

    private lateinit var dbAndPath: Pair<Db, Path>
    private val db get() = dbAndPath.first
    private val path get() = dbAndPath.second

    @BeforeEach
    fun setup() {
        dbAndPath = TestDbFactory.createTempDb("tickets_test")
        TestDbFactory.createDestinationsAndTicketsTables(db)

        TestDbFactory.insertDestination(db, id = 1, name = "Leeds", single = 2.8, ret = 5.6)
        TestDbFactory.insertDestination(db, id = 2, name = "London", single = 28.0, ret = 56.0)
    }

    @AfterEach
    fun tearDown() {
        TestDbFactory.dropTempDb(db, path)
    }

    @Test
    fun recordPurchase_insertsRow_andListRecentReturnsIt() {
        val repo = TicketRepo(db.connection)

        repo.recordPurchase(destinationId = 1, ticketType = "SINGLE", amountDue = 2.8)

        val list = repo.listRecent(limit = 10)
        assertEquals(1, list.size)
        assertEquals(1, list[0].destinationId)
        assertEquals("SINGLE", list[0].ticketType)
        assertEquals(2.8, list[0].amountDue, 0.0001)
        assertTrue(list[0].purchasedAt.isNotBlank())
    }

    @Test
    fun listRecent_returnsNewestFirst() {
        val repo = TicketRepo(db.connection)

        repo.recordPurchase(destinationId = 1, ticketType = "SINGLE", amountDue = 2.8)
        repo.recordPurchase(destinationId = 2, ticketType = "RETURN", amountDue = 56.0)

        val list = repo.listRecent(limit = 10)
        assertEquals(2, list.size)
        assertEquals(2, list[0].destinationId)
        assertEquals(1, list[1].destinationId)
    }

    @Test
    fun countSalesByDestination_countsCorrectly() {
        val repo = TicketRepo(db.connection)

        repo.recordPurchase(destinationId = 1, ticketType = "SINGLE", amountDue = 2.8)
        repo.recordPurchase(destinationId = 1, ticketType = "RETURN", amountDue = 5.6)
        repo.recordPurchase(destinationId = 2, ticketType = "SINGLE", amountDue = 28.0)

        val counts = repo.countSalesByDestination()
        assertEquals(2, counts[1])
        assertEquals(1, counts[2])
    }

    @Test
    fun sumTakingsByDestination_sumsAmountsCorrectly() {
        val repo = TicketRepo(db.connection)

        repo.recordPurchase(destinationId = 1, ticketType = "SINGLE", amountDue = 2.8)
        repo.recordPurchase(destinationId = 1, ticketType = "RETURN", amountDue = 5.6)
        repo.recordPurchase(destinationId = 2, ticketType = "SINGLE", amountDue = 28.0)

        val totals = repo.sumTakingsByDestination()
        assertEquals(8.4, totals[1]!!, 0.0001)
        assertEquals(28.0, totals[2]!!, 0.0001)
    }
}
