package com.group.ticketmachine.db

import java.sql.Connection

object Schema {

    fun create(conn: Connection) {
        conn.createStatement().use { st ->
            st.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS destinations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL
                );
                """.trimIndent()
            )

            st.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS tickets (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    destination_id INTEGER NOT NULL,
                    ticket_type TEXT NOT NULL,
                    amount_due REAL NOT NULL,
                    purchased_at TEXT NOT NULL,
                    FOREIGN KEY(destination_id) REFERENCES destinations(id)
                );
                """.trimIndent()
            )
        }
    }

    fun migrate(conn: Connection) {
        fun hasColumn(table: String, column: String): Boolean {
            conn.createStatement().use { st ->
                st.executeQuery("PRAGMA table_info($table);").use { rs ->
                    while (rs.next()) {
                        if (rs.getString("name").equals(column, ignoreCase = true)) return true
                    }
                }
            }
            return false
        }

        conn.createStatement().use { st ->
            if (!hasColumn("destinations", "single_price")) {
                st.executeUpdate("ALTER TABLE destinations ADD COLUMN single_price REAL;")
            }
            if (!hasColumn("destinations", "return_price")) {
                st.executeUpdate("ALTER TABLE destinations ADD COLUMN return_price REAL;")
            }
        }
    }

    fun seed(conn: Connection) {
        // zostaw jak masz – ważne, żeby seed uzupełniał single_price/return_price jeśli już istnieją
    }
}
