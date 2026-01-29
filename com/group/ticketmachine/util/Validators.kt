package com.group.ticketmachine.util

import java.time.LocalDate

fun requireDateOrder(start: LocalDate, end: LocalDate) {
    require(!end.isBefore(start)) { "End date must not be before start date." }
}
