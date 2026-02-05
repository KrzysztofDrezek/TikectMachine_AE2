package com.group.ticketmachine.db.repo

import java.sql.Connection
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TicketRepo(private val connection: Connection) {

    data class TicketRecord(
        val id: Int,
        val destinationId: Int,
        val ticketType: String,
        val amountDue: Double,
        val purchasedAt: String
    )

    fun recordPurchase(destinationId: Int, ticketType: String, amountDue: Double) {
        val sql = """
            INSERT INTO tickets(destination_id, ticket_type, amount_due, purchased_at)
            VALUES (?, ?, ?, ?)
        """.trimIndent()

        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        connection.prepareStatement(sql).use { ps ->
            ps.setInt(1, destinationId)
            ps.setString(2, ticketType)
            ps.setDouble(3, amountDue)
            ps.setString(4, now)
            ps.executeUpdate()
        }
    }

    fun listRecent(limit: Int = 20): List<TicketRecord> {
        val sql = """
            SELECT id, destination_id, ticket_type, amount_due, purchased_at
            FROM tickets
            ORDER BY id DESC
            LIMIT ?
        """.trimIndent()

        connection.prepareStatement(sql).use { ps ->
            ps.setInt(1, limit)
            ps.executeQuery().use { rs ->
                val out = mutableListOf<TicketRecord>()
                while (rs.next()) {
                    out.add(
                        TicketRecord(
                            id = rs.getInt("id"),
                            destinationId = rs.getInt("destination_id"),
                            ticketType = rs.getString("ticket_type"),
                            amountDue = rs.getDouble("amount_due"),
                            purchasedAt = rs.getString("purchased_at")
                        )
                    )
                }
                return out
            }
        }
    }

    // ✅ NEW: for admin sales per destination
    fun countSalesByDestination(): Map<Int, Int> {
        val sql = """
            SELECT destination_id, COUNT(*) AS c
            FROM tickets
            GROUP BY destination_id
        """.trimIndent()

        connection.prepareStatement(sql).use { ps ->
            ps.executeQuery().use { rs ->
                val map = mutableMapOf<Int, Int>()
                while (rs.next()) {
                    map[rs.getInt("destination_id")] = rs.getInt("c")
                }
                return map
            }
        }
    }
}
