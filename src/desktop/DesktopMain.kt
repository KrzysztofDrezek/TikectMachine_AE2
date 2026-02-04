package com.group.ticketmachine.desktop

import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.group.ticketmachine.auth.LoginService
import com.group.ticketmachine.db.Db
import com.group.ticketmachine.db.Schema
import com.group.ticketmachine.db.repo.DestinationRepo
import com.group.ticketmachine.db.repo.TicketRepo
import com.group.ticketmachine.gui.App
import com.group.ticketmachine.gui.BuyScreen
import com.group.ticketmachine.gui.HistoryScreen
import com.group.ticketmachine.gui.HomeScreen
import com.group.ticketmachine.gui.LoginScreen
import com.group.ticketmachine.model.Destination
import java.nio.file.Files
import java.nio.file.Paths

private enum class Screen {
    HOME, BUY, ADMIN, LOGIN, HISTORY
}

fun main() = application {
    val dataDir = Paths.get("data")
    Files.createDirectories(dataDir)

    val db = Db(dbPath = dataDir.resolve("ticketmachine.db"))
    Schema.create(db.connection)
    Schema.seed(db.connection)

    val destinationRepo = DestinationRepo(db.connection)
    val ticketRepo = TicketRepo(db.connection)

    val loginService = remember { LoginService() }

    var destinations by remember { mutableStateOf<List<Destination>>(emptyList()) }
    var purchases by remember { mutableStateOf<List<TicketRepo.TicketRecord>>(emptyList()) }

    var isAdminLoggedIn by remember { mutableStateOf(false) }
    var screen by remember { mutableStateOf(Screen.HOME) }

    fun refreshDestinations() {
        destinations = destinationRepo.listAll()
    }

    fun refreshPurchases() {
        purchases = ticketRepo.listRecent(limit = 20)
    }

    LaunchedEffect(Unit) {
        refreshDestinations()
        refreshPurchases()
    }

    Window(onCloseRequest = ::exitApplication, title = "TicketMachine") {
        when (screen) {

            Screen.HOME -> {
                HomeScreen(
                    onBuy = { screen = Screen.BUY },
                    onAdmin = { screen = if (isAdminLoggedIn) Screen.ADMIN else Screen.LOGIN },
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
                    onConfirmPurchase = { destination, ticketType, amountDue ->
                        ticketRepo.recordPurchase(
                            destinationId = destination.id,
                            ticketType = ticketType,
                            amountDue = amountDue
                        )
                        refreshPurchases()
                    }
                )
            }

            Screen.LOGIN -> {
                LoginScreen(
                    loginService = loginService,
                    onBack = { screen = Screen.HOME },
                    onLoginSuccess = {
                        isAdminLoggedIn = true
                        screen = Screen.ADMIN
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
