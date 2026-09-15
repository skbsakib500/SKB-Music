package com.skb.music.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerSheet(
    remainingMs: Long?,
    onPick: (Int) -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF141414)
    ) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text(
                "Sleep Timer",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
            if (remainingMs != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Remaining: ${formatRemaining(remainingMs)}",
                    color = Color(0xFF1DB954),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFF262626))

            listOf(5, 10, 15, 30, 45, 60, 90, 120).forEach { min ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            onPick(min)
                            onDismiss()
                        }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$min minutes", color = Color.White)
                }
            }

            if (remainingMs != null) {
                HorizontalDivider(color = Color(0xFF262626))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            onCancel()
                            onDismiss()
                        }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cancel timer", color = Color(0xFFFF6B6B))
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun formatRemaining(ms: Long): String {
    val s = (ms / 1000).coerceAtLeast(0)
    val m = s / 60
    val sec = s % 60
    return "%d:%02d".format(m, sec)
}
