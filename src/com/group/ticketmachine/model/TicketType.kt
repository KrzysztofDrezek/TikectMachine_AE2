package com.group.ticketmachine.model

enum class TicketType {
    SINGLE,
    RETURN;

    fun displayName(): String = when (this) {
        SINGLE -> "Single"
        RETURN -> "Return"
    }
}
