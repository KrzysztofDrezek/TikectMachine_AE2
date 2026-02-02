package com.group.ticketmachine.db

import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager

class Db(dbPath: Path) {

    val connection: Connection = DriverManager.getConnection("jdbc:sqlite:${dbPath.toAbsolutePath()}")

    init {
        connection.createStatement().use { st ->
            st.execute("PRAGMA foreign_keys = ON;")
        }
    }

    fun close() {
        connection.close()
    }
}

