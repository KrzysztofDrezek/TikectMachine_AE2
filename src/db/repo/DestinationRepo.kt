package com.group.ticketmachine.db.repo

import com.group.ticketmachine.model.Destination
import java.sql.Connection

class DestinationRepo(private val connection: Connection) {

    fun listAll(): List<Destination> {
        val sql = """
            SELECT id, name, single_price, return_price
            FROM destinations
            ORDER BY name COLLATE NOCASE
        """.trimIndent()

        connection.prepareStatement(sql).use { ps ->
            ps.executeQuery().use { rs ->
                val out = mutableListOf<Destination>()
                while (rs.next()) {
                    out.add(
                        Destination(
                            id = rs.getInt("id"),
                            name = rs.getString("name"),
                            singlePrice = rs.getDouble("single_price"),
                            returnPrice = rs.getDouble("return_price")
                        )
                    )
                }
                return out
            }
        }
    }

    fun add(name: String, singlePrice: Double, returnPrice: Double) {
        val sql = "INSERT INTO destinations(name, single_price, return_price) VALUES (?, ?, ?)"
        connection.prepareStatement(sql).use { ps ->
            ps.setString(1, name.trim())
            ps.setDouble(2, singlePrice)
            ps.setDouble(3, returnPrice)
            ps.executeUpdate()
        }
    }

    fun update(id: Int, name: String, singlePrice: Double, returnPrice: Double) {
        val sql = """
            UPDATE destinations
            SET name = ?, single_price = ?, return_price = ?
            WHERE id = ?
        """.trimIndent()

        connection.prepareStatement(sql).use { ps ->
            ps.setString(1, name.trim())
            ps.setDouble(2, singlePrice)
            ps.setDouble(3, returnPrice)
            ps.setInt(4, id)
            ps.executeUpdate()
        }
    }

    fun delete(id: Int) {
        val ticketsCount = connection.prepareStatement(
            "SELECT COUNT(*) AS c FROM tickets WHERE destination_id = ?"
        ).use { ps ->
            ps.setInt(1, id)
            ps.executeQuery().use { rs ->
                if (rs.next()) rs.getInt("c") else 0
            }
        }

        if (ticketsCount > 0) {
            throw IllegalStateException("Cannot delete destination: it has $ticketsCount ticket(s) in history.")
        }

        val sql = "DELETE FROM destinations WHERE id = ?"
        connection.prepareStatement(sql).use { ps ->
            ps.setInt(1, id)
            ps.executeUpdate()
        }
    }

    fun applyFactor(factor: Double) {
        val sql = """
        UPDATE destinations
        SET
            single_price = COALESCE(NULLIF(single_price, 0), NULLIF(price, 0), 0) * ?,
            return_price = COALESCE(
                NULLIF(return_price, 0),
                COALESCE(NULLIF(single_price, 0), NULLIF(price, 0), 0) * 2
            ) * ?
    """.trimIndent()

        connection.prepareStatement(sql).use { ps ->
            ps.setDouble(1, factor)
            ps.setDouble(2, factor)
            ps.executeUpdate()
        }
    }
}
