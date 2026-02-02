package com.group.ticketmachine.model
data class Station(
    val name: String,
    var singlePrice: Double,
    var returnPrice: Double,
    var sales: Int = 0,
    var takings: Double = 0.0
)