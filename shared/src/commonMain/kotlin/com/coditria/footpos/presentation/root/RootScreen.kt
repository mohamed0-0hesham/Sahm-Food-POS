package com.coditria.footpos.presentation.root

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.typography
import com.coditria.footpos.core.navigation.Destination
import com.coditria.footpos.core.navigation.Navigator
import com.coditria.footpos.core.navigation.TabKey
import com.coditria.footpos.presentation.auth.AuthScreen
import com.coditria.footpos.presentation.cart.CartScreen
import com.coditria.footpos.presentation.cart.CartViewModel
import com.coditria.footpos.presentation.cart.DiscountSheet
import com.coditria.footpos.presentation.catalog.CatalogScreen
import com.coditria.footpos.presentation.checkout.CheckoutSheet
import com.coditria.footpos.presentation.orders.OrderDetailScreen
import com.coditria.footpos.presentation.orders.OrderHistoryScreen
import com.coditria.footpos.presentation.product.ProductDetailSheet
import com.coditria.footpos.presentation.receipt.ReceiptSheet
import com.coditria.footpos.presentation.settings.AboutScreen
import com.coditria.footpos.presentation.settings.SettingsScreen
import com.coditria.footpos.presentation.splash.SplashScreen
import com.coditria.footpos.presentation.sync.SyncStatusSheet
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RootScreen(
    rootVm: RootViewModel = koinViewModel(),
    navigator: Navigator = koinInject(),
) {
    val rootState by rootVm.state.collectAsStateWithLifecycle()
    val navState by navigator.state.collectAsStateWithLifecycle()

    // Splash until we know whether a user is signed in — gating here so we never
    // flash the auth screen for a user who's already authenticated. The catalog
    // populates from the local cache, which may be empty offline-first until the
    // background refresh kicked off in RootViewModel completes.
    if (!rootState.authResolved) {
        SplashScreen()
        return
    }

    if (!rootState.isAuthenticated) {
        // Navigator state (modals, stacks) is meaningless until the user is in;
        // the auth screen owns the whole window.
        AuthScreen(onAuthenticated = { /* RootViewModel observes auth and re-renders */ })
        return
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 600.dp
        // statusBarsPadding keeps content below the status bar; navigationBarsPadding sits the
        // tab bar above the system nav bar / home indicator.
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Box(Modifier.weight(1f)) {
                TabContent(
                    tab = navState.activeTab,
                    isTablet = isTablet,
                    stack = navState.stacks[navState.activeTab].orEmpty(),
                    navigator = navigator,
                )
            }
            Box(Modifier.navigationBarsPadding()) {
                TabBar(active = navState.activeTab, pendingSyncCount = rootState.pendingSyncCount, onSelect = navigator::selectTab)
            }
        }
        navState.modal?.let { modal ->
            ModalScrim(onDismiss = navigator::dismissModal) {
                ModalContent(modal = modal, navigator = navigator)
            }
        }
    }
}

@Composable
private fun TabContent(
    tab: TabKey,
    isTablet: Boolean,
    stack: List<Destination.Stacked>,
    navigator: Navigator,
) {
    if (stack.isNotEmpty()) {
        StackContent(top = stack.last(), navigator = navigator)
        return
    }
    when (tab) {
        TabKey.Sell -> CatalogScreen(
            isTablet = isTablet,
            onOpenSyncStatus = { navigator.showModal(Destination.Modal.SyncStatus) },
            onOpenCartPhone = { navigator.push(Destination.Stacked.CartPhone) },
        )
        TabKey.Orders -> OrderHistoryScreen(onOpenOrder = { navigator.push(Destination.Stacked.OrderDetail(it)) })
        TabKey.Settings -> SettingsScreen(onOpenAbout = { navigator.push(Destination.Stacked.About) })
    }
}

@Composable
private fun StackContent(top: Destination.Stacked, navigator: Navigator) {
    when (top) {
        Destination.Stacked.CartPhone -> {
            val vm: CartViewModel = koinViewModel()
            CartScreen(onBack = { navigator.pop() }, viewModel = vm)
        }
        is Destination.Stacked.OrderDetail -> OrderDetailScreen(
            orderId = top.orderId,
            onBack = { navigator.pop() },
            onShowReceipt = { text -> navigator.showModal(Destination.Modal.Receipt(top.orderId, text)) },
        )
        Destination.Stacked.About -> AboutScreen(onBack = { navigator.pop() })
    }
}

@Composable
private fun ModalContent(modal: Destination.Modal, navigator: Navigator) {
    when (modal) {
        Destination.Modal.Checkout -> CheckoutSheet()
        is Destination.Modal.Receipt -> ReceiptSheet(
            printedText = modal.printedText,
            onDone = {
                navigator.dismissModal()
                if (modal.completesCheckout) {
                    // Flow A: end of sale — drop any in-progress screens (e.g. phone Cart) and
                    // land the cashier back on the catalog ready for the next order.
                    navigator.selectTab(TabKey.Sell)
                    navigator.popToRoot()
                }
            },
            onReprint = { /* reprint is triggered from OrderDetailViewModel; nothing to do here */ },
        )
        Destination.Modal.Discount -> DiscountSheet(onDismiss = { navigator.dismissModal() })
        Destination.Modal.SyncStatus -> SyncStatusSheet(onDismiss = { navigator.dismissModal() })
        is Destination.Modal.ProductDetail -> ProductDetailSheet(
            productIdValue = modal.productIdValue,
            onDismiss = { navigator.dismissModal() },
        )
    }
}

@Composable
private fun ModalScrim(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    val colors = PosTheme.colors
    Box(
        Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.30f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(colors.backgroundPrimary)
                .clickable(enabled = false) {},
        ) { content() }
    }
}

@Composable
private fun TabBar(active: TabKey, pendingSyncCount: Long, onSelect: (TabKey) -> Unit) {
    val colors = PosTheme.colors
    Row(
        Modifier.fillMaxWidth().background(colors.backgroundTertiary).padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TabItem(label = "Sell", glyph = "🛒", selected = active == TabKey.Sell, onClick = { onSelect(TabKey.Sell) })
        TabItem(label = "Orders", glyph = "📋", selected = active == TabKey.Orders, badge = pendingSyncCount.takeIf { it > 0 }?.toString(), onClick = { onSelect(TabKey.Orders) })
        TabItem(label = "Settings", glyph = "⚙", selected = active == TabKey.Settings, onClick = { onSelect(TabKey.Settings) })
    }
}

@Composable
private fun TabItem(label: String, glyph: String, selected: Boolean, badge: String? = null, onClick: () -> Unit) {
    val colors = PosTheme.colors
    val color = if (selected) colors.accent else colors.labelSecondary
    Column(
        Modifier.clickable(onClick = onClick).padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box {
            Text(glyph, style = PosTheme.typography.title3, color = color)
            if (badge != null) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(50))
                        .background(colors.warning)
                        .padding(horizontal = 5.dp, vertical = 1.dp),
                ) {
                    Text(badge, style = PosTheme.typography.caption2, color = androidx.compose.ui.graphics.Color.White)
                }
            }
        }
        Text(label, style = PosTheme.typography.caption2, color = color)
    }
}
