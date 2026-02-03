package com.group.ticketmachine.db

import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager

class Db(dbPath: Path) {

    val connection: Connection

    init {
        val parent = dbPath.toAbsolutePath().parent
        if (parent != null) Files.createDirectories(parent)

        connection = DriverManager.getConnection("jdbc:sqlite:${dbPath.toAbsolutePath()}")

        connection.createStatement().use { st ->
            st.execute("PRAGMA foreign_keys = ON;")
            st.execute("PRAGMA journal_mode = WAL;")
            st.execute("PRAGMA synchronous = NORMAL;")
        }
    }

    fun close() {
        connection.close()
    }
}

