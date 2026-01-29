package com.group.ticketmachine.model
data class Ticket(
    val station: Station,
    val type: String, // "single" or "return"
    var price: Double
)