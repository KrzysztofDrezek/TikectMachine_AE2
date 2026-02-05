package com.group.ticketmachine.db

import java.security.MessageDigest
import java.sql.Connection

object Schema {

    fun create(conn: Connection) {
        conn.createStatement().use { st ->
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

            // ✅ Special offers stored in SQLite
            st.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS special_offers (
                    id TEXT PRIMARY KEY,
                    station_name TEXT NOT NULL,
                    description TEXT NOT NULL,
                    start_date TEXT NOT NULL,
                    end_date TEXT NOT NULL
                );
                """.trimIndent()
            )

            // ✅ Admin users stored in SQLite
            st.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS admin_users (
                    username TEXT PRIMARY KEY,
                    password_hash TEXT NOT NULL
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
            // Destinations legacy + current columns
            if (!hasColumn("destinations", "price")) {
                st.executeUpdate("ALTER TABLE destinations ADD COLUMN price REAL NOT NULL DEFAULT 0;")
            }
            if (!hasColumn("destinations", "single_price")) {
                st.executeUpdate("ALTER TABLE destinations ADD COLUMN single_price REAL NOT NULL DEFAULT 0;")
            }
            if (!hasColumn("destinations", "return_price")) {
                st.executeUpdate("ALTER TABLE destinations ADD COLUMN return_price REAL NOT NULL DEFAULT 0;")
            }

            // ✅ Ensure special_offers exists for older DBs
            st.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS special_offers (
                    id TEXT PRIMARY KEY,
                    station_name TEXT NOT NULL,
                    description TEXT NOT NULL,
                    start_date TEXT NOT NULL,
                    end_date TEXT NOT NULL
                );
                """.trimIndent()
            )

            // ✅ Ensure admin_users exists for older DBs
            st.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS admin_users (
                    username TEXT PRIMARY KEY,
                    password_hash TEXT NOT NULL
                );
                """.trimIndent()
            )
        }
    }

    fun seed(conn: Connection) {
        seedDestinations(conn)
        seedAdmin(conn)
    }

    private fun seedDestinations(conn: Connection) {
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
                ps.setDouble(2, singlePrice)   // legacy price == single price
                ps.setDouble(3, singlePrice)
                ps.setDouble(4, returnPrice)
                ps.addBatch()
            }
            ps.executeBatch()
        }
    }

    private fun seedAdmin(conn: Connection) {
        val count = conn.createStatement().use { st ->
            st.executeQuery("SELECT COUNT(*) AS c FROM admin_users;").use { rs ->
                if (rs.next()) rs.getInt("c") else 0
            }
        }
        if (count > 0) return

        val username = "admin"
        val password = "admin123"

        conn.prepareStatement(
            "INSERT INTO admin_users(username, password_hash) VALUES (?, ?);"
        ).use { ps ->
            ps.setString(1, username)
            ps.setString(2, sha256(password))
            ps.executeUpdate()
        }
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
