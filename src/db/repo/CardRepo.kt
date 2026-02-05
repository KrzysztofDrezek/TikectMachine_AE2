package com.group.ticketmachine.db.repo

import java.sql.Connection

class CardRepo(private val connection: Connection) {

    fun getCredit(cardNumber: String): Double? {
        val sql = "SELECT credit FROM cards WHERE card_number = ?"
        connection.prepareStatement(sql).use { ps ->
            ps.setString(1, cardNumber.trim())
            ps.executeQuery().use { rs ->
                return if (rs.next()) rs.getDouble("credit") else null
            }
        }
    }

    fun deduct(cardNumber: String, amount: Double): Boolean {
        val card = cardNumber.trim()
        if (card.isEmpty()) return false
        if (amount <= 0) return false

        connection.autoCommit = false
        try {
            val current = getCredit(card)
            if (current == null || current < amount) {
                connection.rollback()
                return false
            }

            val newCredit = current - amount
            val rows = connection.prepareStatement(
                "UPDATE cards SET credit = ? WHERE card_number = ?"
            ).use { ps ->
                ps.setDouble(1, newCredit)
                ps.setString(2, card)
                ps.executeUpdate()
            }

            if (rows != 1) {
                connection.rollback()
                return false
            }

            connection.commit()
            return true
        } catch (e: Exception) {
            runCatching { connection.rollback() }
            throw e
        } finally {
            runCatching { connection.autoCommit = true }
        }
    }
}
