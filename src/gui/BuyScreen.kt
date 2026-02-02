package com.group.ticketmachine.gui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.group.ticketmachine.model.Destination
import kotlin.math.roundToInt

@Composable
fun BuyScreen(
    destinations: List<Destination>,
    onBack: () -> Unit
) {
    var selected by remember { mutableStateOf<Destination?>(null) }
    var qtyText by remember { mutableStateOf("1") }
    var showDialog by remember { mutableStateOf(false) }

    val qty = qtyText.toIntOrNull()?.coerceIn(1, 99) ?: 1
    val total = (selected?.price ?: 0.0) * qty

    MaterialTheme {
        Column(Modifier.fillMaxSize().padding(16.dp)) {

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("BUY", style = MaterialTheme.typography.headlineSmall)
                OutlinedButton(onClick = onBack) { Text("Back") }
            }

            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(12.dp))

            Text("Select destination", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (destinations.isEmpty()) {
                Text("No destinations available.", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(destinations) { d ->
                        val isSelected = selected?.id == d.id
                        ElevatedCard(
                            onClick = { selected = d },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(d.name)
                                Text("£${d.price}")
                            }
                            if (isSelected) {
                                Text(
                                    "Selected",
                                    modifier = Modifier.padding(start = 12.dp, bottom = 12.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it.filter { ch -> ch.isDigit() }.take(2) },
                    label = { Text("Quantity") },
                    modifier = Modifier.width(140.dp)
                )

                OutlinedTextField(
                    value = if (selected != null) "£${((total * 100).roundToInt() / 100.0)}" else "£0.00",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Total") },
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = { showDialog = true },
                    enabled = selected != null,
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("Purchase")
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    Button(onClick = { showDialog = false }) { Text("OK") }
                },
                title = { Text("Purchase complete") },
                text = {
                    val d = selected
                    if (d == null) {
                        Text("No destination selected.")
                    } else {
                        Text("Bought $qty ticket(s) to ${d.name} for £${((total * 100).roundToInt() / 100.0)}")
                    }
                }
            )
        }
    }
}
