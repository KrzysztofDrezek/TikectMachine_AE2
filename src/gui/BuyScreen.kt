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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyScreen(
    destinations: List<Destination>,
    onBack: () -> Unit,
    onConfirmPurchase: (Destination) -> Unit
) {
    var selected by remember { mutableStateOf<Destination?>(null) }
    var showConfirm by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    val amountDue = selected?.price

    if (showConfirm && selected != null) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Confirm purchase") },
            text = {
                Text(
                    "${selected!!.name} - £${formatMoney(selected!!.price)}"
                )
            },
            confirmButton = {
                Button(onClick = {
                    onConfirmPurchase(selected!!)
                    showConfirm = false
                    showSuccess = true
                }) { Text("Confirm") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showSuccess) {
        AlertDialog(
            onDismissRequest = { showSuccess = false },
            title = { Text("Success") },
            text = { Text("Ticket purchased.") },
            confirmButton = {
                Button(onClick = { showSuccess = false }) { Text("OK") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Buy Ticket") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(destinations) { d ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selected = d
                                showConfirm = true
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${d.name} - £${formatMoney(d.price)}")
                        }
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Amount due:", style = MaterialTheme.typography.labelLarge)
                    Text(
                        if (amountDue != null) "£${formatMoney(amountDue)}" else "£0.00",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                OutlinedButton(onClick = onBack) {
                    Text("Back")
                }
            }
        }
    }
}

private fun formatMoney(value: Double): String {
    return String.format(Locale.UK, "%.2f", value)
}
