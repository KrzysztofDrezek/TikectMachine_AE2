package com.group.ticketmachine.gui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.group.ticketmachine.model.Destination

@Composable
fun BuyScreen(
    destinations: List<Destination>,
    onBack: () -> Unit
) {
    var selected by remember { mutableStateOf<Destination?>(null) }

    MaterialTheme {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Buy Ticket", style = MaterialTheme.typography.headlineSmall)

            if (destinations.isEmpty()) {
                Text("No destinations available.")
            } else {
                destinations.forEach { d ->
                    ElevatedButton(
                        onClick = { selected = d },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("${d.name} - £${d.price}")
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Back")
            }
        }

        if (selected != null) {
            AlertDialog(
                onDismissRequest = { selected = null },
                title = { Text("Confirm Purchase") },
                text = { Text("You selected: ${selected!!.name} (£${selected!!.price})") },
                confirmButton = {
                    Button(onClick = { selected = null }) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { selected = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
