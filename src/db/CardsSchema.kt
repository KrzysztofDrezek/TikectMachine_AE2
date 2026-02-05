package com.group.ticketmachine.db

import java.sql.Connection

object CardsSchema {

    fun create(conn: Connection) {
        conn.createStatement().use { st ->
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

    fun migrate(conn: Connection) {
        create(conn)
    }

    fun seed(conn: Connection) {
        val count = conn.createStatement().use { st ->
            st.executeQuery("SELECT COUNT(*) AS c FROM cards;").use { rs ->
                if (rs.next()) rs.getInt("c") else 0
            }
        }
        if (count > 0) return

        val defaults = listOf(
            "4242424242424242" to 200.00,
            "4000056655665556" to 50.00,
            "5555555555554444" to 120.00,
            "378282246310005" to 80.00
        )

        conn.prepareStatement("INSERT INTO cards(card_number, credit) VALUES (?, ?);").use { ps ->
            defaults.forEach { (number, credit) ->
                ps.setString(1, number)
                ps.setDouble(2, credit)
                ps.addBatch()
            }
            ps.executeBatch()
        }
    }
}
