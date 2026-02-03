package com.group.ticketmachine.gui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.group.ticketmachine.model.Destination
import com.group.ticketmachine.model.TicketType

@Composable
fun BuyScreen(
    destinations: List<Destination>,
    onBack: () -> Unit,
    onConfirmPurchase: (Destination, TicketType, Double) -> Unit
) {
    var selectedDestination by remember { mutableStateOf<Destination?>(null) }
    var selectedType by remember { mutableStateOf(TicketType.SINGLE) }
    var showConfirm by remember { mutableStateOf(false) }

    val amountDue = selectedDestination?.let { d ->
        when (selectedType) {
            TicketType.SINGLE -> d.singlePrice
            TicketType.RETURN -> d.returnPrice
        }
    } ?: 0.0

    MaterialTheme {
        Box(Modifier.fillMaxSize().padding(16.dp)) {
            Column(Modifier.fillMaxSize()) {
                Text("Buy Ticket", style = MaterialTheme.typography.headlineSmall)

                Spacer(Modifier.height(12.dp))

                LazyColumn(
                    Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(destinations) { d ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedDestination = d
                                    selectedType = TicketType.SINGLE
                                    showConfirm = true
                                }
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(18.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(d.name)
                                Text("Single £%.2f | Return £%.2f".format(d.singlePrice, d.returnPrice))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Amount due:")
                        Text("£%.2f".format(amountDue))
                    }

                    OutlinedButton(onClick = onBack) { Text("Back") }
                }
            }

            if (showConfirm && selectedDestination != null) {
                AlertDialog(
                    onDismissRequest = { showConfirm = false },
                    title = { Text("Confirm purchase") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("${selectedDestination!!.name}")

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = selectedType == TicketType.SINGLE,
                                    onClick = { selectedType = TicketType.SINGLE },
                                    label = { Text("Single") }
                                )
                                FilterChip(
                                    selected = selectedType == TicketType.RETURN,
                                    onClick = { selectedType = TicketType.RETURN },
                                    label = { Text("Return") }
                                )
                            }

                            Text("Price: £%.2f (%s)".format(amountDue, selectedType.displayName()))
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val d = selectedDestination!!
                                onConfirmPurchase(d, selectedType, amountDue)
                                showConfirm = false
                                selectedDestination = null
                                selectedType = TicketType.SINGLE
                            }
                        ) { Text("Confirm") }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showConfirm = false }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}
