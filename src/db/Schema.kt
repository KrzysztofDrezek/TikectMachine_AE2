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

            st.execute(
                """
                CREATE TABLE IF NOT EXISTS tickets (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    destination_id INTEGER NOT NULL,
                    destination_name TEXT,
                    price REAL NOT NULL,
                    purchased_at TEXT NOT NULL,
                    FOREIGN KEY(destination_id) REFERENCES destinations(id) ON DELETE RESTRICT
                )
                """.trimIndent()
            )
        }

        migrate(connection)
    }

    private fun migrate(connection: Connection) {
        if (!tableExists(connection, "tickets")) return

        if (!columnExists(connection, "tickets", "destination_name")) {
            connection.createStatement().use { st ->
                st.execute("ALTER TABLE tickets ADD COLUMN destination_name TEXT")
            }
        }

        // Fill destination_name for old rows (if destination_id exists and name is null/empty)
        connection.createStatement().use { st ->
            st.executeUpdate(
                """
                UPDATE tickets
                SET destination_name = (
                    SELECT d.name FROM destinations d WHERE d.id = tickets.destination_id
                )
                WHERE destination_name IS NULL OR destination_name = ''
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

    private fun tableExists(connection: Connection, table: String): Boolean {
        val sql = "SELECT name FROM sqlite_master WHERE type='table' AND name=?"
        return connection.prepareStatement(sql).use { ps ->
            ps.setString(1, table)
            ps.executeQuery().use { rs -> rs.next() }
        }
    }

    private fun columnExists(connection: Connection, table: String, column: String): Boolean {
        val sql = "PRAGMA table_info($table)"
        return connection.createStatement().use { st ->
            st.executeQuery(sql).use { rs ->
                while (rs.next()) {
                    if (rs.getString("name").equals(column, ignoreCase = true)) return true
                }
                false
            }
        }
    }
}
