package com.group.ticketmachine.gui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.group.ticketmachine.model.Destination
import com.group.ticketmachine.model.TicketType

data class PurchaseResult(
    val ok: Boolean,
    val message: String,
    val ticketText: String? = null
)

@Composable
fun BuyScreen(
    destinations: List<Destination>,
    onBack: () -> Unit,
    onConfirmPurchase: (Destination, TicketType, Double, String) -> PurchaseResult
) {
    var selectedDestination by remember { mutableStateOf<Destination?>(null) }
    var ticketType by remember { mutableStateOf(TicketType.SINGLE) }

    var showDialog by remember { mutableStateOf(false) }
    var cardNumber by remember { mutableStateOf("") }

    var result by remember { mutableStateOf<PurchaseResult?>(null) }
    var showResult by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Buy Ticket", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(destinations) { d ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(d.name, style = MaterialTheme.typography.titleMedium)
                            Text("Single: £%.2f".format(d.singlePrice))
                            Text("Return: £%.2f".format(d.returnPrice))
                        }

                        Button(onClick = {
                            selectedDestination = d
                            ticketType = TicketType.SINGLE
                            cardNumber = ""
                            showDialog = true
                        }) { Text("Select") }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(onClick = onBack) { Text("Back") }
        }
    }

    if (showDialog && selectedDestination != null) {
        val d = selectedDestination!!
        val amountDue = when (ticketType) {
            TicketType.SINGLE -> d.singlePrice
            TicketType.RETURN -> d.returnPrice
        }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm purchase") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(d.name, style = MaterialTheme.typography.titleMedium)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = ticketType == TicketType.SINGLE,
                            onClick = { ticketType = TicketType.SINGLE },
                            label = { Text("Single") }
                        )
                        FilterChip(
                            selected = ticketType == TicketType.RETURN,
                            onClick = { ticketType = TicketType.RETURN },
                            label = { Text("Return") }
                        )
                    }

                    Text("Amount due: £%.2f".format(amountDue))

                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { cardNumber = it },
                        label = { Text("Virtual card number") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        "Seeded cards: 4242424242424242 (200), 4000056655665556 (50), 5555555555554444 (120), 378282246310005 (80)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val card = cardNumber.trim()
                    if (card.isEmpty()) {
                        result = PurchaseResult(false, "Enter a card number.")
                        showResult = true
                        return@Button
                    }

                    val r = onConfirmPurchase(d, ticketType, amountDue, card)
                    result = r
                    showResult = true

                    if (r.ok) {
                        showDialog = false
                        selectedDestination = null
                        ticketType = TicketType.SINGLE
                        cardNumber = ""
                    }
                }) { Text("Pay") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showResult && result != null) {
        AlertDialog(
            onDismissRequest = { showResult = false },
            title = { Text(if (result!!.ok) "Success" else "Error") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(result!!.message)
                    result!!.ticketText?.let { ticket ->
                        Divider()
                        Text("Ticket print", fontWeight = FontWeight.Bold)
                        Text(ticket)
                    }
                }
            },
            confirmButton = { Button(onClick = { showResult = false }) { Text("OK") } }
        )
    }
}

private fun TicketType.displayName(): String = when (this) {
    TicketType.SINGLE -> "SINGLE"
    TicketType.RETURN -> "RETURN"
}
