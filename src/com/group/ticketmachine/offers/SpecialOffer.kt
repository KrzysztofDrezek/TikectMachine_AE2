package com.group.ticketmachine.offers

import java.time.LocalDate
import java.util.UUID

data class SpecialOffer(
    val id: String = UUID.randomUUID().toString(),
    val stationName: String,
    val description: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)
