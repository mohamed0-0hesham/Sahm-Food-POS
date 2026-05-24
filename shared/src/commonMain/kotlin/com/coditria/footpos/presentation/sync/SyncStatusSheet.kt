package com.coditria.footpos.presentation.sync

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.components.EmptyState
import com.coditria.footpos.core.designsystem.components.IconChip
import com.coditria.footpos.core.designsystem.components.TertiaryButton
import com.coditria.footpos.core.designsystem.shapes
import com.coditria.footpos.core.designsystem.typography
import com.coditria.footpos.domain.model.SyncOperation
import com.coditria.footpos.domain.model.SyncOperationState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SyncStatusSheet(
    onDismiss: () -> Unit,
    vm: SyncStatusViewModel = koinViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val colors = PosTheme.colors

    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.backgroundPrimary)
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
    ) {
        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp)
                .clip(PosTheme.shapes.pill)
                .background(colors.separator)
                .size(width = 40.dp, height = 4.dp),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Sync", style = PosTheme.typography.title1, color = colors.labelPrimary)
                Text(
                    if (state.online) "Connected" else "Offline — orders queued",
                    style = PosTheme.typography.subhead,
                    color = colors.labelSecondary,
                )
            }
            IconChip(glyph = "✕", onClick = onDismiss)
        }
        Spacer(Modifier.size(20.dp))
        ConnectionPanel(online = state.online)
        Spacer(Modifier.size(16.dp))
        if (state.operations.isEmpty()) {
            EmptyState(
                glyph = "✓",
                title = "All up to date",
                message = "There are no pending sync operations.",
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.operations, key = { it.id }) { op ->
                    OperationRow(op, onRetry = { vm.onRetry(op.id) })
                }
            }
        }
    }
}

@Composable
private fun ConnectionPanel(online: Boolean) {
    val colors = PosTheme.colors
    val tint = if (online) colors.success else colors.warning
    Row(
        Modifier
            .fillMaxWidth()
            .clip(PosTheme.shapes.md)
            .background(tint.copy(alpha = 0.10f))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(tint),
        )
        Spacer(Modifier.size(10.dp))
        Text(
            if (online) "Online" else "Offline",
            style = PosTheme.typography.headline,
            color = colors.labelPrimary,
            modifier = Modifier.weight(1f),
        )
        Text(
            if (online) "Syncing in background" else "Will sync when reconnected",
            style = PosTheme.typography.footnote,
            color = colors.labelSecondary,
        )
    }
}

@Composable
private fun OperationRow(op: SyncOperation, onRetry: () -> Unit) {
    val colors = PosTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(PosTheme.shapes.lg)
            .background(colors.surfaceElevated)
            .border(width = 1.dp, color = colors.separator, shape = PosTheme.shapes.lg)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (op.state) {
            SyncOperationState.PENDING -> Box(
                Modifier.size(10.dp).clip(CircleShape).background(colors.warning),
            )
            SyncOperationState.FAILED -> Text("⚠", style = PosTheme.typography.body, color = colors.destructive)
        }
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "Order #${(op as? SyncOperation.CreateOrder)?.orderId?.value?.takeLast(8) ?: op.id.take(8)}",
                style = PosTheme.typography.headline,
                color = colors.labelPrimary,
            )
            Text(
                "Retries: ${op.retryCount}${op.lastError?.let { " · $it" } ?: ""}",
                style = PosTheme.typography.footnote,
                color = colors.labelSecondary,
            )
        }
        if (op.state == SyncOperationState.FAILED) {
            TertiaryButton(text = "Retry", onClick = onRetry)
        }
    }
}
