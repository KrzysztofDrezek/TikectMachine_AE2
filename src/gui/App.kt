@file:OptIn(ExperimentalMaterial3Api::class)

package com.group.ticketmachine.gui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    onDeleteOffer: (String) -> Unit
) {
    var tabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←") }
                }
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
                    onDeleteOffer = onDeleteOffer
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
    onDeleteOffer: (String) -> Unit
) {
    var stationName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDateText by remember { mutableStateOf("") } // YYYY-MM-DD
    var endDateText by remember { mutableStateOf("") }   // YYYY-MM-DD
    var error by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Special Offers", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = stationName,
            onValueChange = { stationName = it },
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
            value = startDateText,
            onValueChange = { startDateText = it },
            label = { Text("Start date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = endDateText,
            onValueChange = { endDateText = it },
            label = { Text("End date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                error = null

                val start = runCatching { LocalDate.parse(startDateText.trim()) }.getOrNull()
                val end = runCatching { LocalDate.parse(endDateText.trim()) }.getOrNull()

                when {
                    stationName.isBlank() -> error = "Station name is required."
                    description.isBlank() -> error = "Description is required."
                    start == null -> error = "Start date must be in format YYYY-MM-DD."
                    end == null -> error = "End date must be in format YYYY-MM-DD."
                    end.isBefore(start) -> error = "End date cannot be before start date."
                    else -> {
                        onAddOffer(
                            stationName.trim(),
                            description.trim(),
                            start,
                            end
                        )
                        stationName = ""
                        description = ""
                        startDateText = ""
                        endDateText = ""
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Add offer") }

        Spacer(Modifier.height(16.dp))
        Divider()
        Spacer(Modifier.height(16.dp))

        if (specialOffers.isEmpty()) {
            Text("No offers yet.")
            return@Column
        }

        LazyColumn(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(specialOffers) { o ->
                Card {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(o.stationName, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text("Description: ${o.description}")
                            Text("From: ${o.startDate}  To: ${o.endDate}")
                        }
                        OutlinedButton(onClick = { onDeleteOffer(o.id) }) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}
