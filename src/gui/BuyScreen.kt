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
    onConfirmPurchase: (Destination) -> Unit,
    onBack: () -> Unit
) {
    var selected by remember { mutableStateOf<Destination?>(null) }
    val amountDue = selected?.price ?: 0.0

    MaterialTheme {
        Box(Modifier.fillMaxSize().padding(20.dp)) {
            Column(Modifier.fillMaxSize()) {
                Text("Buy Ticket", style = MaterialTheme.typography.headlineMedium)

                Spacer(Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(destinations) { d ->
                        FilledTonalButton(
                            onClick = { selected = d },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 18.dp)
                        ) {
                            Text("${d.name} - £${"%.2f".format(d.price)}")
                        }
                    }
                }
            }

            // Amount due (bottom-left)
            Column(
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Text("Amount due:", style = MaterialTheme.typography.bodyMedium)
                Text("£${"%.2f".format(amountDue)}", style = MaterialTheme.typography.bodyLarge)
            }

            // Back (bottom-right)
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Text("Back")
            }
        }

        val picked = selected
        if (picked != null) {
            AlertDialog(
                onDismissRequest = { selected = null },
                title = { Text("Confirm purchase") },
                text = { Text("Buy ticket to ${picked.name} for £${"%.2f".format(picked.price)}?") },
                confirmButton = {
                    Button(
                        onClick = {
                            onConfirmPurchase(picked)
                            selected = null
                        }
                    ) { Text("Confirm") }
                },
                dismissButton = {
                    OutlinedButton(onClick = { selected = null }) { Text("Cancel") }
                }
            )
        }
    }
}
