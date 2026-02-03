package com.group.ticketmachine.db

import java.sql.Connection

object Schema {

    fun create(connection: Connection) {
        createDestinations(connection)
        createTickets(connection)
        migrateTicketsIfNeeded(connection)
    }

    private fun createDestinations(connection: Connection) {
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

    private fun createTickets(connection: Connection) {
        connection.createStatement().use { st ->
            st.execute(
                """
                CREATE TABLE IF NOT EXISTS tickets (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    destination_id INTEGER NOT NULL,
                    amount_due REAL NOT NULL,
                    purchased_at TEXT NOT NULL DEFAULT (datetime('now')),
                    FOREIGN KEY(destination_id) REFERENCES destinations(id)
                )
                """.trimIndent()
            )
        }
    }

    private fun migrateTicketsIfNeeded(connection: Connection) {
        val columns = connection.createStatement().use { st ->
            st.executeQuery("PRAGMA table_info(tickets)").use { rs ->
                val set = mutableSetOf<String>()
                while (rs.next()) set += rs.getString("name")
                set
            }
        }

        if (columns.isEmpty()) return

        // If older version had destination_name instead of destination_id -> rebuild safely.
        val hasDestinationId = columns.contains("destination_id")
        val hasDestinationName = columns.contains("destination_name")

        if (!hasDestinationId && hasDestinationName) {
            connection.createStatement().use { st ->
                st.execute(
                    """
                    CREATE TABLE IF NOT EXISTS tickets_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        destination_id INTEGER NOT NULL,
                        amount_due REAL NOT NULL,
                        purchased_at TEXT NOT NULL DEFAULT (datetime('now')),
                        FOREIGN KEY(destination_id) REFERENCES destinations(id)
                    )
                    """.trimIndent()
                )
            }

            // Copy data (best-effort): match destination_name -> destinations.id
            connection.createStatement().use { st ->
                st.execute(
                    """
                    INSERT INTO tickets_new (id, destination_id, amount_due, purchased_at)
                    SELECT
                        t.id,
                        d.id,
                        COALESCE(t.amount_due, d.price),
                        COALESCE(t.purchased_at, datetime('now'))
                    FROM tickets t
                    JOIN destinations d ON d.name = t.destination_name
                    """.trimIndent()
                )
            }

            connection.createStatement().use { st ->
                st.execute("DROP TABLE tickets")
                st.execute("ALTER TABLE tickets_new RENAME TO tickets")
            }

            return
        }

        // If amount_due missing -> add it (default 0).
        if (!columns.contains("amount_due")) {
            connection.createStatement().use { st ->
                st.execute("ALTER TABLE tickets ADD COLUMN amount_due REAL NOT NULL DEFAULT 0")
            }
        }

        // If purchased_at missing -> add it (default now).
        if (!columns.contains("purchased_at")) {
            connection.createStatement().use { st ->
                st.execute("ALTER TABLE tickets ADD COLUMN purchased_at TEXT NOT NULL DEFAULT (datetime('now'))")
            }
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
                ps.setString(1, name.trim())
                ps.setDouble(2, price)
                ps.addBatch()
            }
            ps.executeBatch()
        }
    }
}
