package com.group.ticketmachine.gui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.group.ticketmachine.model.Destination

@Composable
fun App(
    destinations: List<Destination>,
    onBack: () -> Unit,
    onAdd: (String, Double, Double) -> Unit,
    onUpdate: (Int, String, Double, Double) -> Unit,
    onDelete: (Int) -> Unit,
    onApplyFactor: (Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var singlePrice by remember { mutableStateOf("") }
    var returnPrice by remember { mutableStateOf("") }
    var factor by remember { mutableStateOf("") }

    MaterialTheme {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Admin - Destinations", style = MaterialTheme.typography.headlineSmall)
                OutlinedButton(onClick = onBack) { Text("Back") }
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
                    value = singlePrice,
                    onValueChange = { singlePrice = it },
                    label = { Text("Single") },
                    modifier = Modifier.width(140.dp)
                )
                OutlinedTextField(
                    value = returnPrice,
                    onValueChange = { returnPrice = it },
                    label = { Text("Return") },
                    modifier = Modifier.width(140.dp)
                )
                Button(onClick = {
                    val sp = singlePrice.replace(",", ".").toDoubleOrNull()
                    val rp = returnPrice.replace(",", ".").toDoubleOrNull()
                    if (name.isNotBlank() && sp != null && rp != null) {
                        onAdd(name, sp, rp)
                        name = ""
                        singlePrice = ""
                        returnPrice = ""
                    }
                }) { Text("Add") }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = factor,
                    onValueChange = { factor = it },
                    label = { Text("Price factor (e.g. 1.10)") },
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    val f = factor.replace(",", ".").toDoubleOrNull()
                    if (f != null) {
                        onApplyFactor(f)
                        factor = ""
                    }
                }) { Text("Apply") }
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
    onUpdate: (Int, String, Double, Double) -> Unit,
    onDelete: (Int) -> Unit
) {
    var editName by remember(destination.id) { mutableStateOf(destination.name) }
    var editSingle by remember(destination.id) { mutableStateOf("%.2f".format(destination.singlePrice)) }
    var editReturn by remember(destination.id) { mutableStateOf("%.2f".format(destination.returnPrice)) }

    Card {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text("Sales: ${destination.salesCount}")

            Spacer(Modifier.height(8.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Name") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = editSingle,
                    onValueChange = { editSingle = it },
                    label = { Text("Single") },
                    modifier = Modifier.width(140.dp)
                )

                OutlinedTextField(
                    value = editReturn,
                    onValueChange = { editReturn = it },
                    label = { Text("Return") },
                    modifier = Modifier.width(140.dp)
                )

                Button(onClick = {
                    val sp = editSingle.replace(",", ".").toDoubleOrNull()
                    val rp = editReturn.replace(",", ".").toDoubleOrNull()
                    if (editName.isNotBlank() && sp != null && rp != null) {
                        onUpdate(destination.id, editName, sp, rp)
                    }
                }) { Text("Save") }

                OutlinedButton(onClick = { onDelete(destination.id) }) { Text("Delete") }
            }
        }
    }
}
