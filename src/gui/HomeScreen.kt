package com.group.ticketmachine.gui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onBuy: () -> Unit,
    onAdmin: () -> Unit,
    onHistory: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("TicketMachine", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        Button(onClick = onBuy, modifier = Modifier.fillMaxWidth()) { Text("Buy") }
        Spacer(Modifier.height(12.dp))

        OutlinedButton(onClick = onHistory, modifier = Modifier.fillMaxWidth()) { Text("History") }
        Spacer(Modifier.height(12.dp))

        OutlinedButton(onClick = onAdmin, modifier = Modifier.fillMaxWidth()) { Text("Admin") }
    }
}
