package com.group.ticketmachine.db.repo

import com.group.ticketmachine.model.Destination
import java.sql.Connection

class DestinationRepo(private val connection: Connection) {

    fun listAll(): List<Destination> {
        val sql = "SELECT id, name, price FROM destinations ORDER BY name"
        return connection.prepareStatement(sql).use { ps ->
            ps.executeQuery().use { rs ->
                val out = mutableListOf<Destination>()
                while (rs.next()) {
                    out += Destination(
                        id = rs.getInt("id"),
                        name = rs.getString("name"),
                        price = rs.getDouble("price")
                    )
                }
                out
            }
        }
    }

    fun add(name: String, price: Double) {
        val sql = "INSERT INTO destinations(name, price) VALUES (?, ?)"
        connection.prepareStatement(sql).use { ps ->
            ps.setString(1, name.trim())
            ps.setDouble(2, price)
            ps.executeUpdate()
        }
    }

    fun update(id: Int, name: String, price: Double) {
        val sql = "UPDATE destinations SET name = ?, price = ? WHERE id = ?"
        connection.prepareStatement(sql).use { ps ->
            ps.setString(1, name.trim())
            ps.setDouble(2, price)
            ps.setInt(3, id)
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
}

