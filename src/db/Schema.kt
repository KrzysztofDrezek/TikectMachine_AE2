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

        ensureTicketsSchema(connection)
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

    private fun ensureTicketsSchema(connection: Connection) {
        val ticketsExists = connection.prepareStatement(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='tickets'"
        ).use { ps ->
            ps.executeQuery().use { rs -> rs.next() }
        }

        if (!ticketsExists) {
            createTickets(connection)
            return
        }

        val cols = mutableSetOf<String>()
        connection.prepareStatement("PRAGMA table_info(tickets)").use { ps ->
            ps.executeQuery().use { rs ->
                while (rs.next()) {
                    cols += rs.getString("name")
                }
            }
        }

        val expected = setOf("id", "destination_id", "amount", "purchased_at")
        if (!cols.containsAll(expected)) {
            connection.createStatement().use { st ->
                st.execute("DROP TABLE IF EXISTS tickets")
            }
            createTickets(connection)
        }
    }

    private fun createTickets(connection: Connection) {
        connection.createStatement().use { st ->
            st.execute(
                """
                CREATE TABLE IF NOT EXISTS tickets (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    destination_id INTEGER NOT NULL,
                    amount REAL NOT NULL,
                    purchased_at TEXT NOT NULL,
                    FOREIGN KEY(destination_id) REFERENCES destinations(id)
                )
                """.trimIndent()
            )
            st.execute("CREATE INDEX IF NOT EXISTS idx_tickets_purchased_at ON tickets(purchased_at)")
            st.execute("CREATE INDEX IF NOT EXISTS idx_tickets_destination_id ON tickets(destination_id)")
        }
    }
}
