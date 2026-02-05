package com.group.ticketmachine

import com.group.ticketmachine.db.Db
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists

object TestDbFactory {

    fun createTempDb(prefix: String): Pair<Db, Path> {
        val dir = Files.createTempDirectory("tm_test_")
        val path = dir.resolve("$prefix.db")
        val db = Db(path)
        return db to path
    }

    fun dropTempDb(db: Db, path: Path) {
        runCatching { db.close() }
        runCatching { path.deleteIfExists() }
        runCatching { path.resolveSibling(path.fileName.toString() + "-wal").deleteIfExists() }
        runCatching { path.resolveSibling(path.fileName.toString() + "-shm").deleteIfExists() }
        runCatching { path.parent?.toFile()?.deleteRecursively() }
    }

    fun createCardsTable(db: Db) {
        db.connection.createStatement().use { st ->
            st.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS cards (
                    card_number TEXT PRIMARY KEY,
                    credit REAL NOT NULL DEFAULT 0
                );
                """.trimIndent()
            )
        }
    }

    fun insertCard(db: Db, cardNumber: String, credit: Double) {
        db.connection.prepareStatement(
            "INSERT INTO cards(card_number, credit) VALUES (?, ?);"
        ).use { ps ->
            ps.setString(1, cardNumber)
            ps.setDouble(2, credit)
            ps.executeUpdate()
        }
    }

    fun createDestinationsAndTicketsTables(db: Db) {
        db.connection.createStatement().use { st ->
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

    fun insertDestination(db: Db, id: Int? = null, name: String, single: Double, ret: Double) {
        val sql = if (id == null) {
            "INSERT INTO destinations(name, price, single_price, return_price) VALUES (?, ?, ?, ?);"
        } else {
            "INSERT INTO destinations(id, name, price, single_price, return_price) VALUES (?, ?, ?, ?, ?);"
        }

        db.connection.prepareStatement(sql).use { ps ->
            var i = 1
            if (id != null) ps.setInt(i++, id)
            ps.setString(i++, name)
            ps.setDouble(i++, single) // legacy price = single
            ps.setDouble(i++, single)
            ps.setDouble(i++, ret)
            ps.executeUpdate()
        }
    }

    fun createSpecialOffersTable(db: Db) {
        db.connection.createStatement().use { st ->
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
        }
    }
}
