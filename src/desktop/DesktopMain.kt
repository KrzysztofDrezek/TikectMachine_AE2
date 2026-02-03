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
import com.group.ticketmachine.gui.HistoryScreen
import com.group.ticketmachine.gui.HomeScreen
import com.group.ticketmachine.model.Destination
import com.group.ticketmachine.db.repo.TicketRepo.TicketRecord
import java.nio.file.Files
import java.nio.file.Paths

private enum class Screen {
    HOME, BUY, ADMIN, HISTORY
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
    var purchases by remember { mutableStateOf<List<TicketRecord>>(emptyList()) }
    var screen by remember { mutableStateOf(Screen.HOME) }

    fun refreshDestinations() {
        destinations = destinationRepo.listAll()
    }

    fun refreshPurchases() {
        purchases = ticketRepo.listRecent(limit = 20)
    }

    fun refreshAll() {
        refreshDestinations()
        refreshPurchases()
    }

    LaunchedEffect(Unit) { refreshAll() }

    Window(onCloseRequest = ::exitApplication, title = "TicketMachine") {
        when (screen) {
            Screen.HOME -> {
                HomeScreen(
                    onBuy = { screen = Screen.BUY },
                    onAdmin = { screen = Screen.ADMIN },
                    onHistory = {
                        refreshPurchases()
                        screen = Screen.HISTORY
                    }
                )
            }

            Screen.BUY -> {
                BuyScreen(
                    destinations = destinations,
                    onBack = { screen = Screen.HOME },
                    onConfirmPurchase = { destination, type, amountDue ->
                        ticketRepo.recordPurchase(
                            destinationId = destination.id,
                            ticketType = type,
                            amountDue = amountDue
                        )
                        refreshPurchases()
                    }
                )
            }

            Screen.ADMIN -> {
                App(
                    destinations = destinations,
                    onBack = { screen = Screen.HOME },
                    onAdd = { name, singlePrice, returnPrice ->
                        destinationRepo.add(name, singlePrice, returnPrice)
                        refreshDestinations()
                    },
                    onUpdate = { id, name, singlePrice, returnPrice ->
                        destinationRepo.update(id, name, singlePrice, returnPrice)
                        refreshDestinations()
                    },
                    onDelete = { id ->
                        destinationRepo.delete(id)
                        refreshDestinations()
                    },
                    onApplyFactor = { factor ->
                        destinationRepo.applyFactor(factor)
                        refreshDestinations()
                    }
                )
            }

            Screen.HISTORY -> {
                HistoryScreen(
                    purchases = purchases,
                    onBack = { screen = Screen.HOME }
                )
            }
        }
    }
}
