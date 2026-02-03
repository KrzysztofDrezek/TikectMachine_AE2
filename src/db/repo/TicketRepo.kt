package com.group.ticketmachine.db.repo

import com.group.ticketmachine.model.TicketPurchase
import java.sql.Connection

class TicketRepo(private val connection: Connection) {

    fun add(destinationId: Int, amount: Double, purchasedAt: String) {
        val sql = "INSERT INTO tickets(destination_id, amount, purchased_at) VALUES (?, ?, ?)"
        connection.prepareStatement(sql).use { ps ->
            ps.setInt(1, destinationId)
            ps.setDouble(2, amount)
            ps.setString(3, purchasedAt)
            ps.executeUpdate()
        }
    }

    fun listAll(): List<TicketPurchase> {
        val sql =
            """
            SELECT 
                t.id,
                d.name AS destination_name,
                t.amount,
                t.purchased_at
            FROM tickets t
            JOIN destinations d ON d.id = t.destination_id
            ORDER BY t.id DESC
            """.trimIndent()

        return connection.prepareStatement(sql).use { ps ->
            ps.executeQuery().use { rs ->
                val out = mutableListOf<TicketPurchase>()
                while (rs.next()) {
                    out += TicketPurchase(
                        id = rs.getInt("id"),
                        destinationName = rs.getString("destination_name"),
                        amount = rs.getDouble("amount"),
                        purchasedAt = rs.getString("purchased_at")
                    )
                }
                out
            }
        }
    }
}
