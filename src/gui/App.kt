@file:OptIn(ExperimentalMaterial3Api::class)

package com.group.ticketmachine.gui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.group.ticketmachine.model.Destination

@Composable
fun App(
    destinations: List<Destination>,
    onBack: () -> Unit,
    onAdd: (String, Double) -> Unit,
    onUpdate: (Int, String, Double) -> Unit,
    onDelete: (Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    MaterialTheme {
        Column(Modifier.fillMaxSize().padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }

                Text("Admin: Destinations", style = MaterialTheme.typography.headlineSmall)
            }

            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") },
                    modifier = Modifier.width(140.dp)
                )

                Button(
                    onClick = {
                        val p = price.replace(",", ".").toDoubleOrNull()
                        if (name.isNotBlank() && p != null) {
                            onAdd(name, p)
                            name = ""
                            price = ""
                        }
                    }
                ) { Text("Add") }
            }

            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(12.dp))

            LazyColumn(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(destinations) { d ->
                    DestinationRow(d, onUpdate, onDelete)
                }
            }
        }
    }
}

@Composable
private fun DestinationRow(
    destination: Destination,
    onUpdate: (Int, String, Double) -> Unit,
    onDelete: (Int) -> Unit
) {
    var editName by remember(destination.id) { mutableStateOf(destination.name) }
    var editPrice by remember(destination.id) { mutableStateOf(destination.price.toString()) }

    Card {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = editName,
                onValueChange = { editName = it },
                label = { Text("Name") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = editPrice,
                onValueChange = { editPrice = it },
                label = { Text("Price") },
                modifier = Modifier.width(140.dp)
            )

            Button(
                onClick = {
                    val p = editPrice.replace(",", ".").toDoubleOrNull()
                    if (editName.isNotBlank() && p != null) {
                        onUpdate(destination.id, editName, p)
                    }
                }
            ) { Text("Save") }

            OutlinedButton(onClick = { onDelete(destination.id) }) { Text("Delete") }
        }
    }
}

