package com.group.ticketmachine.desktop

import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.group.ticketmachine.auth.LoginService
import com.group.ticketmachine.core.InMemoryStationProvider
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
import com.group.ticketmachine.offers.SpecialOffer
import com.group.ticketmachine.offers.SpecialOfferRepository
import com.group.ticketmachine.offers.SpecialOfferService
import com.group.ticketmachine.auth.AdminUserRepo
import java.nio.file.Files
import java.nio.file.Paths

private enum class Screen {
    HOME, BUY, LOGIN, ADMIN, HISTORY
}

fun main() = application {
    val dataDir = Paths.get("data")
    Files.createDirectories(dataDir)

    val db = Db(dbPath = dataDir.resolve("ticketmachine.db"))
    Schema.create(db.connection)
    Schema.migrate(db.connection)
    Schema.seed(db.connection)

    val destinationRepo = DestinationRepo(db.connection)
    val ticketRepo = TicketRepo(db.connection)

    val loginService = remember { LoginService(AdminUserRepo(db.connection)) }

    val stationProvider = remember { InMemoryStationProvider() }
    val offerRepo = remember { SpecialOfferRepository(db.connection) }
    val offerService = remember { SpecialOfferService(offerRepo, stationProvider) }

    var destinations by remember { mutableStateOf<List<Destination>>(emptyList()) }
    var purchases by remember { mutableStateOf<List<TicketRepo.TicketRecord>>(emptyList()) }
    var specialOffers by remember { mutableStateOf<List<SpecialOffer>>(emptyList()) }

    // ✅ NEW: sales count map for admin destinations view
    var salesByDestinationId by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }

    var isAdminLoggedIn by remember { mutableStateOf(false) }
    var screen by remember { mutableStateOf(Screen.HOME) }

    fun refreshDestinations() {
        destinations = destinationRepo.listAll()
        salesByDestinationId = ticketRepo.countSalesByDestination()
    }

    fun refreshPurchases() {
        purchases = ticketRepo.listRecent(limit = 20)
        salesByDestinationId = ticketRepo.countSalesByDestination()
    }

    fun loadAllOffers() {
        specialOffers = offerService.listAll()
    }

    fun searchOffersByStation(station: String) {
        specialOffers = offerService.searchByStation(station)
    }

    fun deleteOfferByAnyId(input: String): Boolean {
        if (offerService.deleteById(input)) {
            loadAllOffers()
            return true
        }

        val all = offerService.listAll()
        val matches = all.filter { it.id.startsWith(input) }
        if (matches.size == 1) {
            val ok = offerService.deleteById(matches.first().id)
            loadAllOffers()
            return ok
        }

        return false
    }

    LaunchedEffect(Unit) {
        refreshDestinations()
        refreshPurchases()
        loadAllOffers()
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
                            ticketType = ticketType.toString(),
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
                    salesByDestinationId = salesByDestinationId,
                    specialOffers = specialOffers,
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
                    },

                    onAddOffer = { stationName, description, startDate, endDate ->
                        offerService.addOffer(stationName, description, startDate, endDate)
                        loadAllOffers()
                    },
                    onDeleteOffer = { id ->
                        offerService.deleteById(id)
                        loadAllOffers()
                    },
                    onSearchOffersByStation = { station ->
                        searchOffersByStation(station)
                    },
                    onDeleteOfferByAnyId = { input ->
                        deleteOfferByAnyId(input)
                    },
                    onListAllOffers = {
                        loadAllOffers()
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
