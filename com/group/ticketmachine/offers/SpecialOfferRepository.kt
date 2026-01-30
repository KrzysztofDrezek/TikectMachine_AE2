package com.group.ticketmachine.offers

class SpecialOfferRepository {
    private val offers = mutableListOf<SpecialOffer>()

    fun add(offer: SpecialOffer) { offers += offer }
    fun all(): List<SpecialOffer> = offers.toList()
    fun findByStation(stationQuery: String): List<SpecialOffer> =
        offers.filter { it.stationName.contains(stationQuery, ignoreCase = true) }

    fun deleteById(id: String): Boolean = offers.removeIf { it.id == id }
}
