@file:OptIn(ExperimentalMaterial3Api::class)

package com.group.ticketmachine.gui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.group.ticketmachine.model.Destination
import com.group.ticketmachine.offers.SpecialOffer
import java.time.LocalDate

@Composable
fun App(
    destinations: List<Destination>,
    specialOffers: List<SpecialOffer>,
    onBack: () -> Unit,

    onAdd: (String, Double, Double) -> Unit,
    onUpdate: (Int, String, Double, Double) -> Unit,
    onDelete: (Int) -> Unit,
    onApplyFactor: (Double) -> Unit,

    onAddOffer: (String, String, LocalDate, LocalDate) -> Unit,
    onDeleteOffer: (String) -> Unit,

    // POINT 8
    onSearchOffersByStation: (String) -> Unit,

    // POINT 9
    onDeleteOfferByAnyId: (String) -> Boolean
) {
    var tabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel") },
                navigationIcon = { IconButton(onClick = onBack) { Text("←") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding)
        ) {
            TabRow(selectedTabIndex = tabIndex) {
                Tab(
                    selected = tabIndex == 0,
                    onClick = { tabIndex = 0 },
                    text = { Text("Destinations") }
                )
                Tab(
                    selected = tabIndex == 1,
                    onClick = { tabIndex = 1 },
                    text = { Text("Special Offers") }
                )
            }

            when (tabIndex) {
                0 -> DestinationsAdmin(
                    destinations = destinations,
                    onAdd = onAdd,
                    onUpdate = onUpdate,
                    onDelete = onDelete,
                    onApplyFactor = onApplyFactor
                )

                1 -> SpecialOffersAdmin(
                    specialOffers = specialOffers,
                    onAddOffer = onAddOffer,
                    onDeleteOffer = onDeleteOffer,
                    onSearchOffersByStation = onSearchOffersByStation,
                    onDeleteOfferByAnyId = onDeleteOfferByAnyId
                )
            }
        }
    }
}

@Composable
private fun DestinationsAdmin(
    destinations: List<Destination>,
    onAdd: (String, Double, Double) -> Unit,
    onUpdate: (Int, String, Double, Double) -> Unit,
    onDelete: (Int) -> Unit,
    onApplyFactor: (Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var singlePrice by remember { mutableStateOf("") }
    var returnPrice by remember { mutableStateOf("") }
    var factor by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Manage Destinations", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = singlePrice,
                onValueChange = { singlePrice = it },
                label = { Text("Single price") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = returnPrice,
                onValueChange = { returnPrice = it },
                label = { Text("Return price") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                val sp = singlePrice.replace(",", ".").toDoubleOrNull()
                val rp = returnPrice.replace(",", ".").toDoubleOrNull()
                if (name.isNotBlank() && sp != null && rp != null) {
                    onAdd(name, sp, rp)
                    name = ""
                    singlePrice = ""
                    returnPrice = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Add destination") }

        Spacer(Modifier.height(16.dp))
        Divider()
        Spacer(Modifier.height(16.dp))

        Text("Apply price factor (e.g. 1.1 = +10%)", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = factor,
                onValueChange = { factor = it },
                label = { Text("Factor") },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    val f = factor.replace(",", ".").toDoubleOrNull()
                    if (f != null && f > 0.0) {
                        onApplyFactor(f)
                        factor = ""
                    }
                }
            ) { Text("Apply") }
        }

        Spacer(Modifier.height(16.dp))
        Divider()
        Spacer(Modifier.height(16.dp))

        LazyColumn(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(destinations) { d ->
                DestinationRow(
                    destination = d,
                    onUpdate = onUpdate,
                    onDelete = onDelete
                )
            }
        }
    }
}

@Composable
private fun DestinationRow(
    destination: Destination,
    onUpdate: (Int, String, Double, Double) -> Unit,
    onDelete: (Int) -> Unit
) {
    var editName by remember(destination.id) { mutableStateOf(destination.name) }
    var editSingle by remember(destination.id) { mutableStateOf(destination.singlePrice.toString()) }
    var editReturn by remember(destination.id) { mutableStateOf(destination.returnPrice.toString()) }

    Card {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            OutlinedTextField(
                value = editName,
                onValueChange = { editName = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = editSingle,
                    onValueChange = { editSingle = it },
                    label = { Text("Single") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = editReturn,
                    onValueChange = { editReturn = it },
                    label = { Text("Return") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val sp = editSingle.replace(",", ".").toDoubleOrNull()
                        val rp = editReturn.replace(",", ".").toDoubleOrNull()
                        if (editName.isNotBlank() && sp != null && rp != null) {
                            onUpdate(destination.id, editName, sp, rp)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Save") }

                OutlinedButton(
                    onClick = { onDelete(destination.id) },
                    modifier = Modifier.weight(1f)
                ) { Text("Delete") }
            }
        }
    }
}

@Composable
private fun SpecialOffersAdmin(
    specialOffers: List<SpecialOffer>,
    onAddOffer: (String, String, LocalDate, LocalDate) -> Unit,
    onDeleteOffer: (String) -> Unit,

    // POINT 8
    onSearchOffersByStation: (String) -> Unit,

    // POINT 9
    onDeleteOfferByAnyId: (String) -> Boolean
) {
    var station by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var endDate by remember { mutableStateOf(LocalDate.now().plusDays(7).toString()) }

    // POINT 8
    var stationQuery by remember { mutableStateOf("") }

    // POINT 9
    var deleteId by remember { mutableStateOf("") }
    var statusMsg by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Special Offers", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        // Add offer
        OutlinedTextField(
            value = station,
            onValueChange = { station = it },
            label = { Text("Station name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = startDate,
            onValueChange = { startDate = it },
            label = { Text("Start date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = endDate,
            onValueChange = { endDate = it },
            label = { Text("End date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                statusMsg = null
                runCatching {
                    val s = LocalDate.parse(startDate.trim())
                    val e = LocalDate.parse(endDate.trim())
                    if (station.isNotBlank() && description.isNotBlank()) {
                        onAddOffer(station, description, s, e)
                        station = ""
                        description = ""
                    }
                }.onFailure {
                    statusMsg = "Invalid date format. Use YYYY-MM-DD."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Add offer") }

        Spacer(Modifier.height(16.dp))
        Divider()
        Spacer(Modifier.height(16.dp))

        // POINT 8: Search
        Text("Search offers by station", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = stationQuery,
            onValueChange = { stationQuery = it },
            label = { Text("Station name (search)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                statusMsg = null
                val q = stationQuery.trim()
                if (q.isEmpty()) statusMsg = "Enter a station name to search."
                else onSearchOffersByStation(q)
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Search") }

        Spacer(Modifier.height(16.dp))
        Divider()
        Spacer(Modifier.height(16.dp))

        // POINT 9: Delete by ID
        Text("Delete offer by ID", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = deleteId,
            onValueChange = { deleteId = it },
            label = { Text("Offer ID (full or short prefix)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                statusMsg = null
                val input = deleteId.trim()
                if (input.isEmpty()) {
                    statusMsg = "Enter an ID (full or short)."
                    return@Button
                }
                val ok = onDeleteOfferByAnyId(input)
                statusMsg = if (ok) "Offer deleted." else "Not found, or short ID is ambiguous."
                if (ok) deleteId = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Delete by ID") }

        if (statusMsg != null) {
            Spacer(Modifier.height(12.dp))
            Text(statusMsg!!, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(16.dp))
        Divider()
        Spacer(Modifier.height(16.dp))

        // List (ID is selectable with mouse)
        LazyColumn(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(specialOffers) { offer ->
                Card {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(offer.stationName, style = MaterialTheme.typography.titleMedium)
                            Text("Description: ${offer.description}")
                            Text("From: ${offer.startDate}  To: ${offer.endDate}")

                            Spacer(Modifier.height(6.dp))

                            SelectionContainer {
                                Column {
                                    Text("Short ID: ${offer.id.take(8)}", style = MaterialTheme.typography.bodySmall)
                                    Text("Full ID: ${offer.id}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        OutlinedButton(onClick = { onDeleteOffer(offer.id) }) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}
