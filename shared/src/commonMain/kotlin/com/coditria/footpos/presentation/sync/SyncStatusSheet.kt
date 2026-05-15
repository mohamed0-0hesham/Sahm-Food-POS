package com.coditria.footpos.presentation.sync

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
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
import com.coditria.footpos.core.designsystem.components.TertiaryButton
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

    Column(Modifier.fillMaxWidth().background(colors.backgroundPrimary).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Sync Status", style = PosTheme.typography.title2, color = colors.labelPrimary, modifier = Modifier.weight(1f))
            Text("✕", style = PosTheme.typography.title3, color = colors.labelSecondary, modifier = Modifier.clickable { onDismiss() }.padding(4.dp))
        }
        Spacer(Modifier.size(16.dp))
        ConnectionRow(online = state.online)
        Spacer(Modifier.size(16.dp))
        if (state.operations.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.CheckCircle,
                title = "All up to date",
                message = "There are no pending sync operations.",
            )
        } else {
            LazyColumn(contentPadding = PaddingValues(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.operations, key = { it.id }) { op -> OperationRow(op, onRetry = { vm.onRetry(op.id) }) }
            }
        }
    }
}

@Composable
private fun ConnectionRow(online: Boolean) {
    val colors = PosTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.backgroundSecondary)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (online) {
            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = colors.success)
        } else {
            Icon(Icons.Rounded.CloudOff, contentDescription = null, tint = colors.labelSecondary)
        }
        Spacer(Modifier.size(8.dp))
        Text(if (online) "Online" else "Offline", style = PosTheme.typography.headline, color = colors.labelPrimary)
    }
}

@Composable
private fun OperationRow(op: SyncOperation, onRetry: () -> Unit) {
    val colors = PosTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.backgroundSecondary)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (op.state) {
            SyncOperationState.PENDING -> Box(Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(colors.warning))
            SyncOperationState.FAILED -> Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = colors.destructive)
        }
        Spacer(Modifier.size(8.dp))
        Column(Modifier.weight(1f)) {
            Text("Order ${(op as? SyncOperation.CreateOrder)?.orderId?.value?.takeLast(8) ?: op.id.take(8)}", style = PosTheme.typography.headline, color = colors.labelPrimary)
            Text("Retries: ${op.retryCount}${op.lastError?.let { " · $it" } ?: ""}", style = PosTheme.typography.footnote, color = colors.labelSecondary)
        }
        if (op.state == SyncOperationState.FAILED) {
            TertiaryButton(text = "Retry", onClick = onRetry)
        }
    }
}

@Suppress("unused")
@Composable
private fun Divider() {
    val colors = PosTheme.colors
    Box(Modifier.fillMaxWidth().height(1.dp).background(colors.separator))
}
