package com.group.ticketmachine.offers

import com.group.ticketmachine.core.StationProvider
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SpecialOfferServiceTest {

    private class FakeStations(private val stations: List<String>) : StationProvider {
        override fun allStations(): List<String> = stations
    }

    @Test
    fun addOffer_rejectsUnknownStation() {
        val repo = SpecialOfferRepository(null) // in-memory mode
        val stations = FakeStations(listOf("London", "Leeds"))
        val service = SpecialOfferService(repo, stations)

        val ex = assertThrows(IllegalArgumentException::class.java) {
            service.addOffer(
                stationName = "Manchester",
                description = "10% off",
                start = LocalDate.parse("2026-02-01"),
                end = LocalDate.parse("2026-02-10")
            )
        }
        assertTrue(ex.message?.contains("Unknown station", ignoreCase = true) == true)
    }

    @Test
    fun addOffer_rejectsInvalidDateOrder() {
        val repo = SpecialOfferRepository(null)
        val stations = FakeStations(listOf("London"))
        val service = SpecialOfferService(repo, stations)

        assertThrows(IllegalArgumentException::class.java) {
            service.addOffer(
                stationName = "London",
                description = "10% off",
                start = LocalDate.parse("2026-02-10"),
                end = LocalDate.parse("2026-02-01")
            )
        }
    }

    @Test
    fun addOffer_addsOffer_andSearchWorks() {
        val repo = SpecialOfferRepository(null)
        val stations = FakeStations(listOf("London", "Leeds"))
        val service = SpecialOfferService(repo, stations)

        service.addOffer(
            stationName = "London",
            description = "10% off",
            start = LocalDate.parse("2026-02-01"),
            end = LocalDate.parse("2026-02-10")
        )

        val all = service.listAll()
        assertEquals(1, all.size)

        val found = service.searchByStation("lon")
        assertEquals(1, found.size)
        assertEquals("London", found[0].stationName)
    }
}
