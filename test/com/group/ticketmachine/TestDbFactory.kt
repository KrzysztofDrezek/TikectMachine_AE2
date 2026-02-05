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
}
