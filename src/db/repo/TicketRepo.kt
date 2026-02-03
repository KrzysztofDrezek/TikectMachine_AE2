package com.group.ticketmachine.db.repo

import java.sql.Connection
import java.time.Instant

class TicketRepo(private val connection: Connection) {

    fun addPurchase(destinationId: Int, destinationName: String, price: Double) {
        val sql =
            """
            INSERT INTO tickets(destination_id, destination_name, price, purchased_at)
            VALUES (?, ?, ?, ?)
            """.trimIndent()

        connection.prepareStatement(sql).use { ps ->
            ps.setInt(1, destinationId)
            ps.setString(2, destinationName.trim())
            ps.setDouble(3, price)
            ps.setString(4, Instant.now().toString())
            ps.executeUpdate()
        }
    }
}
