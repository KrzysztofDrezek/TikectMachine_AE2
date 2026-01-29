package com.group.ticketmachine.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateParsers {
    val df: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    fun parse(s: String): LocalDate = LocalDate.parse(s, df)
}
