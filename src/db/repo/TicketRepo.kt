package com.group.ticketmachine.db.repo

import com.group.ticketmachine.model.TicketType
import java.sql.Connection

class TicketRepo(private val connection: Connection) {

    data class TicketRecord(
        val id: Int,
        val destinationName: String,
        val ticketType: TicketType,
        val amountDue: Double,
        val purchasedAt: String
    )

    fun recordPurchase(destinationId: Int, ticketType: TicketType, amountDue: Double) {
        val sql = """
            INSERT INTO tickets(destination_id, ticket_type, amount_due)
            VALUES (?, ?, ?)
        """.trimIndent()

        connection.prepareStatement(sql).use { ps ->
            ps.setInt(1, destinationId)
            ps.setString(2, ticketType.name)
            ps.setDouble(3, amountDue)
            ps.executeUpdate()
        }
    }

    fun listRecent(limit: Int = 20): List<TicketRecord> {
        val sql = """
            SELECT
                t.id,
                d.name AS destination_name,
                t.ticket_type,
                t.amount_due,
                t.purchased_at
            FROM tickets t
            JOIN destinations d ON d.id = t.destination_id
            ORDER BY t.purchased_at DESC, t.id DESC
            LIMIT ?
        """.trimIndent()

        return connection.prepareStatement(sql).use { ps ->
            ps.setInt(1, limit)
            ps.executeQuery().use { rs ->
                val out = mutableListOf<TicketRecord>()
                while (rs.next()) {
                    val typeRaw = rs.getString("ticket_type") ?: "SINGLE"
                    val type = runCatching { TicketType.valueOf(typeRaw) }.getOrDefault(TicketType.SINGLE)

                    out += TicketRecord(
                        id = rs.getInt("id"),
                        destinationName = rs.getString("destination_name"),
                        ticketType = type,
                        amountDue = rs.getDouble("amount_due"),
                        purchasedAt = rs.getString("purchased_at")
                    )
                }
                out
            }
        }
    }
}
