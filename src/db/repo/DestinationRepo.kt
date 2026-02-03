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
        val sql = "DELETE FROM destinations WHERE id = ?"
        connection.prepareStatement(sql).use { ps ->
            ps.setInt(1, id)
            ps.executeUpdate()
        }
    }

    fun applyFactor(factor: Double) {
        val safeFactor = factor
        val sql = "UPDATE destinations SET return_price = single_price * ?"
        connection.prepareStatement(sql).use { ps ->
            ps.setDouble(1, safeFactor)
            ps.executeUpdate()
        }
    }
}
