package com.group.ticketmachine.db

import java.sql.Connection

object Schema {

    fun create(connection: Connection) {
        connection.createStatement().use { st ->
            st.execute("PRAGMA foreign_keys = ON;")

            // Base table creation (new schema)
            st.execute(
                """
                CREATE TABLE IF NOT EXISTS destinations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE,
                    single_price REAL NOT NULL,
                    return_price REAL NOT NULL
                )
                """.trimIndent()
            )

            // Purchases history
            st.execute(
                """
                CREATE TABLE IF NOT EXISTS ticket_purchases (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    destination_id INTEGER NOT NULL,
                    ticket_type TEXT NOT NULL,
                    amount_due REAL NOT NULL,
                    purchased_at TEXT NOT NULL,
                    FOREIGN KEY(destination_id) REFERENCES destinations(id) ON DELETE CASCADE
                )
                """.trimIndent()
            )
        }

        migrate(connection)
    }

    private fun migrate(connection: Connection) {
        // If old project created destinations(name, price) we add new columns and copy values.
        val hasSingle = hasColumn(connection, "destinations", "single_price")
        val hasReturn = hasColumn(connection, "destinations", "return_price")
        val hasOldPrice = hasColumn(connection, "destinations", "price")

        connection.createStatement().use { st ->
            if (!hasSingle) {
                st.execute("ALTER TABLE destinations ADD COLUMN single_price REAL NOT NULL DEFAULT 0;")
            }
            if (!hasReturn) {
                st.execute("ALTER TABLE destinations ADD COLUMN return_price REAL NOT NULL DEFAULT 0;")
            }

            // Copy from legacy "price" if it exists and new columns were missing previously.
            // (Safe to run even if already copied; it only fills zeros.)
            if (hasOldPrice) {
                st.execute(
                    """
                    UPDATE destinations
                    SET
                        single_price = CASE WHEN single_price = 0 THEN price ELSE single_price END,
                        return_price = CASE WHEN return_price = 0 THEN price * 2 ELSE return_price END
                    """
                        .trimIndent()
                )
            }
        }
    }

    private fun hasColumn(connection: Connection, table: String, column: String): Boolean {
        val sql = "PRAGMA table_info($table)"
        connection.createStatement().use { st ->
            st.executeQuery(sql).use { rs ->
                while (rs.next()) {
                    val name = rs.getString("name")
                    if (name.equals(column, ignoreCase = true)) return true
                }
            }
        }
        return false
    }

    fun seed(connection: Connection) {
        val count = connection.createStatement().use { st ->
            st.executeQuery("SELECT COUNT(*) FROM destinations").use { rs ->
                rs.next()
                rs.getInt(1)
            }
        }
        if (count > 0) return

        val sql = "INSERT INTO destinations(name, single_price, return_price) VALUES (?, ?, ?)"
        connection.prepareStatement(sql).use { ps ->
            listOf(
                Triple("Leeds", 2.8, 5.6),
                Triple("London", 28.0, 56.0),
                Triple("Manchester", 6.5, 13.0)
            ).forEach { (name, single, ret) ->
                ps.setString(1, name)
                ps.setDouble(2, single)
                ps.setDouble(3, ret)
                ps.addBatch()
            }
            ps.executeBatch()
        }
    }
}
