package com.group.ticketmachine.offers

import com.group.ticketmachine.util.requireDateOrder
import com.group.ticketmachine.core.StationProvider
import java.time.LocalDate

class SpecialOfferService(
    private val repo: SpecialOfferRepository,
    private val stations: StationProvider
) {
    fun addOffer(stationName: String, description: String, start: LocalDate, end: LocalDate): SpecialOffer {
        require(stations.allStations().any { it.equals(stationName, ignoreCase = true) }) {
            "Unknown station: $stationName"
        }
        requireDateOrder(start, end)
        val offer = SpecialOffer(
            stationName = stationName.trim(),
            description = description.trim(),
            startDate = start, endDate = end
        )
        repo.add(offer)
        return offer
    }

    fun listAll(): List<SpecialOffer> = repo.all()
    fun searchByStation(query: String): List<SpecialOffer> = repo.findByStation(query)
    fun deleteById(id: String): Boolean = repo.deleteById(id)
}
