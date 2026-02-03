package com.group.ticketmachine.db

import java.sql.Connection

object Schema {

    fun create(connection: Connection) {
        connection.createStatement().use { st ->
            st.execute("PRAGMA foreign_keys = ON;")
            st.execute("PRAGMA journal_mode = WAL;")
            st.execute("PRAGMA synchronous = NORMAL;")

            st.execute(
                """
                CREATE TABLE IF NOT EXISTS destinations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE,
                    price REAL NOT NULL CHECK(price >= 0)
                );
                """.trimIndent()
            )

            st.execute(
                """
                CREATE TABLE IF NOT EXISTS tickets (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    destination_id INTEGER NOT NULL,
                    quantity INTEGER NOT NULL CHECK(quantity > 0),
                    total_price REAL NOT NULL CHECK(total_price >= 0),
                    purchased_at TEXT NOT NULL,
                    FOREIGN KEY(destination_id) REFERENCES destinations(id) ON DELETE RESTRICT
                );
                """.trimIndent()
            )

            st.execute("CREATE INDEX IF NOT EXISTS idx_tickets_destination_id ON tickets(destination_id);")
            st.execute("CREATE INDEX IF NOT EXISTS idx_tickets_purchased_at ON tickets(purchased_at);")
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
                "Leeds" to 2.80,
                "London" to 28.00,
                "Manchester" to 6.50,
                "York" to 4.20,
                "Sheffield" to 5.10
            ).forEach { (name, price) ->
                ps.setString(1, name)
                ps.setDouble(2, price)
                ps.addBatch()
            }
            ps.executeBatch()
        }
    }
}




