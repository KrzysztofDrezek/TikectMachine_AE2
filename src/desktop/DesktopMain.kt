package com.group.ticketmachine.desktop

import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.group.ticketmachine.db.Db
import com.group.ticketmachine.db.Schema
import com.group.ticketmachine.db.repo.DestinationRepo
import com.group.ticketmachine.db.repo.TicketRepo
import com.group.ticketmachine.gui.App
import com.group.ticketmachine.gui.BuyScreen
import com.group.ticketmachine.gui.HomeScreen
import com.group.ticketmachine.model.Destination
import java.nio.file.Files
import java.nio.file.Paths

private enum class Screen {
    HOME, BUY, ADMIN
}

fun main() = application {
    val dataDir = Paths.get("data")
    Files.createDirectories(dataDir)

    val db = Db(dbPath = dataDir.resolve("ticketmachine.db"))
    Schema.create(db.connection)
    Schema.seed(db.connection)

    val destinationRepo = DestinationRepo(db.connection)
    val ticketRepo = TicketRepo(db.connection)

    var destinations by remember { mutableStateOf<List<Destination>>(emptyList()) }
    var screen by remember { mutableStateOf(Screen.HOME) }

    fun refreshDestinations() {
        destinations = destinationRepo.listAll()
    }

    LaunchedEffect(Unit) { refreshDestinations() }

    Window(onCloseRequest = ::exitApplication, title = "TicketMachine") {
        when (screen) {
            Screen.HOME -> {
                HomeScreen(
                    onBuy = { screen = Screen.BUY },
                    onAdmin = { screen = Screen.ADMIN }
                )
            }

            Screen.BUY -> {
                BuyScreen(
                    destinations = destinations,
                    onBack = { screen = Screen.HOME },
                    onConfirmPurchase = { destination ->
                        ticketRepo.insertPurchase(destinationId = destination.id, amountDue = destination.price)
                    }
                )
            }

            Screen.ADMIN -> {
                App(
                    destinations = destinations,
                    onBack = { screen = Screen.HOME },
                    onAdd = { name, price ->
                        destinationRepo.add(name, price)
                        refreshDestinations()
                    },
                    onUpdate = { id, name, price ->
                        destinationRepo.update(id, name, price)
                        refreshDestinations()
                    },
                    onDelete = { id ->
                        destinationRepo.delete(id)
                        refreshDestinations()
                    }
                )
            }
        }
    }
}
