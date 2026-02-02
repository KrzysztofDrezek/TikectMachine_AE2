package com.group.ticketmachine.desktop

import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.group.ticketmachine.db.Db
import com.group.ticketmachine.db.Schema
import com.group.ticketmachine.db.repo.DestinationRepo
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

    val repo = DestinationRepo(db.connection)

    var screen by remember { mutableStateOf(Screen.HOME) }
    var destinations by remember { mutableStateOf<List<Destination>>(emptyList()) }

    fun refresh() {
        destinations = repo.listAll()
    }

    LaunchedEffect(Unit) { refresh() }

    Window(onCloseRequest = ::exitApplication, title = "TicketMachine") {

        when (screen) {
            Screen.HOME -> HomeScreen(
                onBuy = {
                    refresh()
                    screen = Screen.BUY
                },
                onAdmin = {
                    refresh()
                    screen = Screen.ADMIN
                }
            )

            Screen.BUY -> BuyScreen(
                destinations = destinations,
                onBack = { screen = Screen.HOME }
            )

            Screen.ADMIN -> App(
                destinations = destinations,
                onAdd = { name, price ->
                    repo.add(name, price)
                    refresh()
                },
                onUpdate = { id, name, price ->
                    repo.update(id, name, price)
                    refresh()
                },
                onDelete = { id ->
                    repo.delete(id)
                    refresh()
                }
            )
        }
    }
}

