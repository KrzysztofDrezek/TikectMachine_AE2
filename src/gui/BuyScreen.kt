package com.group.ticketmachine.gui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.group.ticketmachine.model.Destination

@Composable
fun BuyScreen(
    destinations: List<Destination>,
    onBack: () -> Unit,
    onConfirmPurchase: (Destination) -> Unit
) {
    var selected by remember { mutableStateOf<Destination?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().padding(16.dp)) {
        Column(Modifier.fillMaxSize()) {
            Text("Buy Ticket", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))

            LazyColumn(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(destinations) { d ->
                    Button(
                        onClick = {
                            selected = d
                            showDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Text("${d.name} - £${"%.2f".format(d.price)}")
                    }
                }
            }

            selected?.let { s ->
                Spacer(Modifier.height(12.dp))
                Text("Amount due:")
                Text("£${"%.2f".format(s.price)}", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(onClick = onBack) { Text("Back") }
            }
        }

        if (showDialog && selected != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Confirm purchase") },
                text = {
                    Text(
                        "Buy a ticket to ${selected!!.name} for £${"%.2f".format(selected!!.price)}?"
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        onConfirmPurchase(selected!!)
                        showDialog = false
                    }) { Text("Confirm") }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}
