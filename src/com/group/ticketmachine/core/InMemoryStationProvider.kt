package com.group.ticketmachine.core

class InMemoryStationProvider : StationProvider {
    private val stations = listOf(
        "Leeds", "York", "Manchester", "London", "Birmingham", "Sheffield"
    )
    override fun allStations(): List<String> = stations
}
