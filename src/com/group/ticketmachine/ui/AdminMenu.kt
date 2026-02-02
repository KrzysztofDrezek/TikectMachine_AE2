package com.group.ticketmachine.db.ticketmachine.ui


import com.group.ticketmachine.offers.SpecialOfferService
import com.group.ticketmachine.util.ConsoleIO
import java.time.format.DateTimeFormatter

class AdminMenu(private val offerService: SpecialOfferService) {
    private val df: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun run() {
        while (true) {
            println(
                """
                ---- ADMIN MENU ----
                1) Add special offer
                2) Search offers by station
                3) Delete offer by ID
                4) List all offers
                5) Back / Logout
                """.trimIndent()
            )

            when (ConsoleIO.prompt("Choose: ")) {
                "1" -> addOffer()
                "2" -> search()
                "3" -> delete()
                "4" -> listAll()
                "5" -> return
                else -> println("Unknown option.")
            }
        }
    }

    private fun addOffer() {
        val station = ConsoleIO.readNonEmpty("Station name: ")
        val desc = ConsoleIO.readNonEmpty("Description: ")
        val start = ConsoleIO.readDate("Start date")
        val end = ConsoleIO.readDate("End date")

        try {
            val offer = offerService.addOffer(station, desc, start, end)
            println("Added offer: ${shortId(offer.id)} (full: ${offer.id})")
        } catch (e: IllegalArgumentException) {
            println("Error: ${e.message}")
        }
    }

    private fun search() {
        val q = ConsoleIO.readNonEmpty("Search station: ")
        val results = offerService.searchByStation(q)

        if (results.isEmpty()) {
            println("No offers.")
        } else {
            results.forEach { offer ->
                println(
                    "${shortId(offer.id)} | ${offer.stationName} | ${offer.description} | " +
                            "${offer.startDate.format(df)} → ${offer.endDate.format(df)}"
                )
            }
            println("Tip: use the short ID above, or paste the full UUID when deleting.")
        }
    }

    private fun delete() {
        val input = ConsoleIO.readNonEmpty("Offer ID to delete (short or full): ")
        val ok = deleteByShortOrFullId(input)
        println(if (ok) "Deleted." else "Not found (or ambiguous short ID).")
    }

    private fun listAll() {
        val all = offerService.listAll()

        if (all.isEmpty()) {
            println("No offers.")
        } else {
            all.forEach { offer ->
                println(
                    "${shortId(offer.id)} | ${offer.stationName} | ${offer.description} | " +
                            "${offer.startDate.format(df)} → ${offer.endDate.format(df)}"
                )
            }
            println("Tip: use the short ID above, or paste the full UUID when deleting.")
        }
    }

    private fun shortId(id: String): String = id.take(8)

    private fun deleteByShortOrFullId(input: String): Boolean {
        // If user pasted full UUID, try direct delete first
        if (offerService.deleteById(input)) return true

        // Otherwise treat it as a short ID prefix and try to match exactly one offer
        val matches = offerService.listAll().filter { it.id.startsWith(input) }

        return when (matches.size) {
            1 -> offerService.deleteById(matches.first().id)
            else -> false // 0 or >1 matches
        }
    }
}
