package com.group.ticketmachine.model

import com.group.ticketmachine.offers.SpecialOffer

class TicketMachine(
    val stations: MutableList<Station> = mutableListOf(
        Station("Colchester", 18.50, 33.00),
        Station("Leeds", 29.80, 55.00),
        // ...
    ),
    var currentBalance: Double = 0.0,
    var selectedTicket: Ticket? = null,
    val originStation: String = "London Central",
    val specialOffers: MutableList<SpecialOffer> = mutableListOf()
)
