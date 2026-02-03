package com.group.ticketmachine.db.repo

import java.sql.Connection

class TicketRepo(private val connection: Connection) {

    fun insertPurchase(destinationId: Int, amountDue: Double) {
        val sql = "INSERT INTO tickets(destination_id, amount_due) VALUES (?, ?)"
        connection.prepareStatement(sql).use { ps ->
            ps.setInt(1, destinationId)
            ps.setDouble(2, amountDue)
            ps.executeUpdate()
        }
    }
}
