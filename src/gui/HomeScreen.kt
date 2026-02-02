package com.group.ticketmachine.gui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onBuy: () -> Unit,
    onAdmin: () -> Unit
) {
    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.widthIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("TicketMachine", style = MaterialTheme.typography.headlineMedium)

                Text(
                    "Choose a mode",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onBuy,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("BUY")
                }

                OutlinedButton(
                    onClick = onAdmin,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("ADMIN")
                }
            }
        }
    }
}
