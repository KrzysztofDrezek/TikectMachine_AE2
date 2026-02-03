package com.group.ticketmachine.model

data class TicketPurchase(
    val id: Int,
    val destinationName: String,
    val amount: Double,
    val purchasedAt: String
)
