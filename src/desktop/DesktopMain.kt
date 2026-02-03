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
import com.group.ticketmachine.model.TicketPurchase
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
    var purchases by remember { mutableStateOf<List<TicketPurchase>>(emptyList()) }
    var screen by remember { mutableStateOf(Screen.HOME) }

    fun refreshDestinations() {
        destinations = destinationRepo.listAll()
    }

    fun refreshHistory() {
        purchases = ticketRepo.listAll()
    }

    LaunchedEffect(Unit) {
        refreshDestinations()
        refreshHistory()
    }

    DisposableEffect(Unit) {
        onDispose { db.close() }
    }

    Window(onCloseRequest = ::exitApplication, title = "TicketMachine") {
        when (screen) {
            Screen.HOME -> HomeScreen(
                onBuy = { screen = Screen.BUY },
                onAdmin = { screen = Screen.ADMIN },
                onHistory = {
                    refreshHistory()
                    screen = Screen.HISTORY
                }
            )

            Screen.BUY -> BuyScreen(
                destinations = destinations,
                onBack = { screen = Screen.HOME },
                onConfirmPurchase = { d ->
                    val ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    ticketRepo.add(destinationId = d.id, amount = d.price, purchasedAt = ts)
                    refreshHistory()
                }
            )

            Screen.ADMIN -> App(
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

            Screen.HISTORY -> HistoryScreen(
                purchases = purchases,
                onBack = { screen = Screen.HOME }
            )
        }
    }
}
