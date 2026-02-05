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
import com.group.ticketmachine.offers.SpecialOffer
import java.time.LocalDate
import kotlin.math.max

data class PurchaseResult(
    val ok: Boolean,
    val message: String,
    val ticketText: String? = null
)

@Composable
fun BuyScreen(
    destinations: List<Destination>,
    specialOffers: List<SpecialOffer>,
    onBack: () -> Unit,
    onConfirmPurchase: (Destination, TicketType, Double, String) -> PurchaseResult
) {
    var selectedDestination by remember { mutableStateOf<Destination?>(null) }
    var ticketType by remember { mutableStateOf(TicketType.SINGLE) }

    var showDialog by remember { mutableStateOf(false) }
    var cardNumber by remember { mutableStateOf("") }

    var result by remember { mutableStateOf<PurchaseResult?>(null) }
    var showResult by remember { mutableStateOf(false) }

    val today = LocalDate.now()

    fun basePrice(d: Destination, type: TicketType): Double =
        when (type) {
            TicketType.SINGLE -> d.singlePrice
            TicketType.RETURN -> d.returnPrice
        }

    fun priceWithOffers(d: Destination, type: TicketType): Pair<Double, String?> {
        val base = basePrice(d, type)

        val active = specialOffers
            .filter { it.stationName.equals(d.name, ignoreCase = true) }
            .filter { !today.isBefore(it.startDate) && !today.isAfter(it.endDate) }
            .filter { appliesToType(it.description, type) } // ✅ apply offer only for matching ticket type

        if (active.isEmpty()) return base to null

        var bestPrice = base
        var bestLabel: String? = null

        active.forEach { offer ->
            val p = applyOffer(base, offer.description)
            if (p < bestPrice) {
                bestPrice = p
                bestLabel = offer.description
            }
        }

        bestPrice = max(0.0, bestPrice)
        return bestPrice to bestLabel
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Buy Ticket", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(destinations) { d ->
                val single = priceWithOffers(d, TicketType.SINGLE).first
                val ret = priceWithOffers(d, TicketType.RETURN).first

                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(d.name, style = MaterialTheme.typography.titleMedium)
                            Text("Single: £%.2f".format(single))
                            Text("Return: £%.2f".format(ret))
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
        val (amountDue, offerLabel) = priceWithOffers(d, ticketType)

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
                    if (offerLabel != null) {
                        Text("Offer applied: $offerLabel", style = MaterialTheme.typography.bodySmall)
                    }

                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { cardNumber = it },
                        label = { Text("Virtual card number") },
                        modifier = Modifier.fillMaxWidth()
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

private fun appliesToType(description: String, type: TicketType): Boolean {
    val d = description.lowercase()

    val mentionsSingle = d.contains("single")
    val mentionsReturn = d.contains("return")

    // If description mentions neither type -> applies to both
    if (!mentionsSingle && !mentionsReturn) return true

    return when (type) {
        TicketType.SINGLE -> mentionsSingle
        TicketType.RETURN -> mentionsReturn
    }
}

private fun applyOffer(base: Double, description: String): Double {
    val d = description.trim().lowercase()

    // price override: "price=12.34"
    Regex("""price\s*=\s*([0-9]+([.,][0-9]+)?)""").find(d)?.let { m ->
        val v = m.groupValues[1].replace(",", ".").toDoubleOrNull()
        if (v != null) return v
    }

    // percent off: "10% off"
    Regex("""([0-9]+([.,][0-9]+)?)\s*%""").find(d)?.let { m ->
        val pct = m.groupValues[1].replace(",", ".").toDoubleOrNull()
        if (pct != null) return base * (1.0 - (pct / 100.0))
    }

    // amount off: "£5 off" or "5£ off"
    Regex("""£\s*([0-9]+([.,][0-9]+)?)""").find(d)?.let { m ->
        val off = m.groupValues[1].replace(",", ".").toDoubleOrNull()
        if (off != null) return max(0.0, base - off)
    }
    Regex("""([0-9]+([.,][0-9]+)?)\s*£""").find(d)?.let { m ->
        val off = m.groupValues[1].replace(",", ".").toDoubleOrNull()
        if (off != null) return max(0.0, base - off)
    }

    // fallback: no change
    return base
}
