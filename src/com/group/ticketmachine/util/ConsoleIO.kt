package com.group.ticketmachine.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ConsoleIO {
        private val df: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        fun prompt(text: String): String {
            print(text)
            return readln().trim()
        }

        fun readNonEmpty(promptText: String): String {
            while (true) {
                val v = prompt(promptText)
                if (v.isNotBlank()) return v
                println("Value cannot be empty.")
            }
        }

        fun readDate(promptText: String): LocalDate {
            while (true) {
                val s = readNonEmpty("$promptText (yyyy-MM-dd): ")
                try {
                    return LocalDate.parse(s, df)
                } catch (_: Exception) {
                    println("Invalid date format. Use yyyy-MM-dd.")
                }
            }
        }
    }
