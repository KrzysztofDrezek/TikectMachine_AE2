package com.group.ticketmachine.db

import java.sql.Connection

object Schema {

    fun create(connection: Connection) {
        connection.createStatement().use { st ->
            st.execute(
                """
                CREATE TABLE IF NOT EXISTS destinations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE,
                    price REAL NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    fun seed(connection: Connection) {
        val count = connection.createStatement().use { st ->
            st.executeQuery("SELECT COUNT(*) FROM destinations").use { rs ->
                rs.next()
                rs.getInt(1)
            }
        }

        if (count > 0) return

        val sql = "INSERT INTO destinations(name, price) VALUES (?, ?)"
        connection.prepareStatement(sql).use { ps ->
            listOf(
                "Leeds" to 2.8,
                "London" to 28.0,
                "Manchester" to 6.5
            ).forEach { (name, price) ->
                ps.setString(1, name)
                ps.setDouble(2, price)
                ps.addBatch()
            }
            ps.executeBatch()
        }
    }
}




