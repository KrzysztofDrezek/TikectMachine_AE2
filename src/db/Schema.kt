package com.group.ticketmachine.db

import java.sql.Connection

object Schema {

    fun create(conn: Connection) {
        conn.createStatement().use { st ->
            // Keep schema compatible with older DB: name UNIQUE + legacy "price" column
            st.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS destinations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE,
                    price REAL NOT NULL DEFAULT 0,
                    return_price REAL NOT NULL DEFAULT 0,
                    single_price REAL NOT NULL DEFAULT 0
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
            if (!hasColumn("destinations", "price")) {
                st.executeUpdate("ALTER TABLE destinations ADD COLUMN price REAL NOT NULL DEFAULT 0;")
            }
            if (!hasColumn("destinations", "single_price")) {
                st.executeUpdate("ALTER TABLE destinations ADD COLUMN single_price REAL NOT NULL DEFAULT 0;")
            }
            if (!hasColumn("destinations", "return_price")) {
                st.executeUpdate("ALTER TABLE destinations ADD COLUMN return_price REAL NOT NULL DEFAULT 0;")
            }
        }
    }

    fun seed(conn: Connection) {
        val count = conn.createStatement().use { st ->
            st.executeQuery("SELECT COUNT(*) AS c FROM destinations;").use { rs ->
                if (rs.next()) rs.getInt("c") else 0
            }
        }

        // Seed only when table is empty (prevents duplicates and preserves user data)
        if (count > 0) return

        // Defaults from your old ticketmachine.db
        val defaults = listOf(
            Triple("Leeds", 2.8, 5.6),
            Triple("London", 28.0, 56.0),
            Triple("Manchester", 6.5, 13.0)
        )

        conn.prepareStatement(
            "INSERT INTO destinations(name, price, single_price, return_price) VALUES (?, ?, ?, ?);"
        ).use { ps ->
            defaults.forEach { (name, singlePrice, returnPrice) ->
                ps.setString(1, name)
                ps.setDouble(2, singlePrice)   // legacy price == single price in old DB
                ps.setDouble(3, singlePrice)
                ps.setDouble(4, returnPrice)
                ps.addBatch()
            }
            ps.executeBatch()
        }
    }
}
