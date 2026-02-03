package com.group.ticketmachine.db.repo

import java.sql.Connection
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class TicketRow(
    val id: Int,
    val destinationName: String,
    val quantity: Int,
    val totalPrice: Double,
    val purchasedAt: String
)

class TicketRepo(private val connection: Connection) {

    private val dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun purchase(destinationId: Int, quantity: Int, totalPrice: Double) {
        val sql = """
            INSERT INTO tickets(destination_id, quantity, total_price, purchased_at)
            VALUES (?, ?, ?, ?)
        """.trimIndent()

        connection.prepareStatement(sql).use { ps ->
            ps.setInt(1, destinationId)
            ps.setInt(2, quantity)
            ps.setDouble(3, totalPrice)
            ps.setString(4, LocalDateTime.now().format(dtf))
            ps.executeUpdate()
        }
    }

    fun listHistory(limit: Int = 50): List<TicketRow> {
        val sql = """
            SELECT t.id, d.name AS destination_name, t.quantity, t.total_price, t.purchased_at
            FROM tickets t
            JOIN destinations d ON d.id = t.destination_id
            ORDER BY t.purchased_at DESC
            LIMIT ?
        """.trimIndent()

        return connection.prepareStatement(sql).use { ps ->
            ps.setInt(1, limit)
            ps.executeQuery().use { rs ->
                val out = mutableListOf<TicketRow>()
                while (rs.next()) {
                    out += TicketRow(
                        id = rs.getInt("id"),
                        destinationName = rs.getString("destination_name"),
                        quantity = rs.getInt("quantity"),
                        totalPrice = rs.getDouble("total_price"),
                        purchasedAt = rs.getString("purchased_at")
                    )
                }
                out
            }
        }
    }
}
