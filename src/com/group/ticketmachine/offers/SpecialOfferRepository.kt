package com.group.ticketmachine.offers

import java.sql.Connection
import java.time.LocalDate

class SpecialOfferRepository(private val connection: Connection? = null) {

    private val offers = mutableListOf<SpecialOffer>()

    fun add(offer: SpecialOffer) {
        val conn = connection
        if (conn == null) {
            offers += offer
            return
        }

        val sql = """
            INSERT INTO special_offers(id, station_name, description, start_date, end_date)
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()

        conn.prepareStatement(sql).use { ps ->
            ps.setString(1, offer.id)
            ps.setString(2, offer.stationName)
            ps.setString(3, offer.description)
            ps.setString(4, offer.startDate.toString())
            ps.setString(5, offer.endDate.toString())
            ps.executeUpdate()
        }
    }

    fun all(): List<SpecialOffer> {
        val conn = connection
        if (conn == null) return offers.toList()

        val sql = """
            SELECT id, station_name, description, start_date, end_date
            FROM special_offers
            ORDER BY start_date DESC, station_name COLLATE NOCASE
        """.trimIndent()

        conn.prepareStatement(sql).use { ps ->
            ps.executeQuery().use { rs ->
                val out = mutableListOf<SpecialOffer>()
                while (rs.next()) {
                    out += SpecialOffer(
                        id = rs.getString("id"),
                        stationName = rs.getString("station_name"),
                        description = rs.getString("description"),
                        startDate = LocalDate.parse(rs.getString("start_date")),
                        endDate = LocalDate.parse(rs.getString("end_date"))
                    )
                }
                return out
            }
        }
    }

    fun findByStation(stationQuery: String): List<SpecialOffer> {
        val q = stationQuery.trim()
        val conn = connection
        if (conn == null) {
            return offers.filter { it.stationName.contains(q, ignoreCase = true) }
        }

        val sql = """
            SELECT id, station_name, description, start_date, end_date
            FROM special_offers
            WHERE LOWER(station_name) LIKE '%' || LOWER(?) || '%'
            ORDER BY start_date DESC, station_name COLLATE NOCASE
        """.trimIndent()

        conn.prepareStatement(sql).use { ps ->
            ps.setString(1, q)
            ps.executeQuery().use { rs ->
                val out = mutableListOf<SpecialOffer>()
                while (rs.next()) {
                    out += SpecialOffer(
                        id = rs.getString("id"),
                        stationName = rs.getString("station_name"),
                        description = rs.getString("description"),
                        startDate = LocalDate.parse(rs.getString("start_date")),
                        endDate = LocalDate.parse(rs.getString("end_date"))
                    )
                }
                return out
            }
        }
    }

    fun deleteById(id: String): Boolean {
        val conn = connection
        if (conn == null) {
            return offers.removeIf { it.id == id }
        }

        val sql = "DELETE FROM special_offers WHERE id = ?"
        conn.prepareStatement(sql).use { ps ->
            ps.setString(1, id)
            val rows = ps.executeUpdate()
            return rows > 0
        }
    }
}
