package com.group.ticketmachine.model

data class Destination(
    val id: Int,
    val name: String,
    val singlePrice: Double,
    val returnPrice: Double,
    val salesCount: Int = 0
)
