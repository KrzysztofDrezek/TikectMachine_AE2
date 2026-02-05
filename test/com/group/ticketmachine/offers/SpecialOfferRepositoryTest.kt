package com.group.ticketmachine.offers

import com.group.ticketmachine.TestDbFactory
import com.group.ticketmachine.db.Db
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.time.LocalDate

class SpecialOfferRepositoryTest {

    private lateinit var dbAndPath: Pair<Db, Path>
    private val db get() = dbAndPath.first
    private val path get() = dbAndPath.second

    @BeforeEach
    fun setup() {
        dbAndPath = TestDbFactory.createTempDb("offers_repo_test")
        TestDbFactory.createSpecialOffersTable(db)
    }

    @AfterEach
    fun tearDown() {
        TestDbFactory.dropTempDb(db, path)
    }

    @Test
    fun add_and_all_returnsInsertedOffers() {
        val repo = SpecialOfferRepository(db.connection)

        val offer = SpecialOffer(
            id = "id-1",
            stationName = "London",
            description = "10% off",
            startDate = LocalDate.parse("2026-02-01"),
            endDate = LocalDate.parse("2026-02-10")
        )
        repo.add(offer)

        val all = repo.all()
        assertEquals(1, all.size)
        assertEquals("id-1", all[0].id)
        assertEquals("London", all[0].stationName)
        assertEquals("10% off", all[0].description)
    }

    @Test
    fun findByStation_isCaseInsensitive_andUsesContains() {
        val repo = SpecialOfferRepository(db.connection)

        repo.add(
            SpecialOffer(
                id = "id-1",
                stationName = "London",
                description = "10% off",
                startDate = LocalDate.parse("2026-02-01"),
                endDate = LocalDate.parse("2026-02-10")
            )
        )
        repo.add(
            SpecialOffer(
                id = "id-2",
                stationName = "Leeds",
                description = "5% off",
                startDate = LocalDate.parse("2026-02-01"),
                endDate = LocalDate.parse("2026-02-10")
            )
        )

        val found = repo.findByStation("lon")
        assertEquals(1, found.size)
        assertEquals("London", found[0].stationName)

        val found2 = repo.findByStation("LON")
        assertEquals(1, found2.size)
        assertEquals("London", found2[0].stationName)
    }

    @Test
    fun deleteById_removesExistingOffer() {
        val repo = SpecialOfferRepository(db.connection)

        repo.add(
            SpecialOffer(
                id = "id-1",
                stationName = "London",
                description = "10% off",
                startDate = LocalDate.parse("2026-02-01"),
                endDate = LocalDate.parse("2026-02-10")
            )
        )

        assertTrue(repo.deleteById("id-1"))
        assertTrue(repo.all().isEmpty())
        assertFalse(repo.deleteById("id-1"))
    }
}
