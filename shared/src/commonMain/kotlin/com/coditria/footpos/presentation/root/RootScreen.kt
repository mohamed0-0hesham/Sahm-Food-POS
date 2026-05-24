package com.coditria.footpos.presentation.root

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.coditria.footpos.core.designsystem.PosMotion
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.shapes
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
    val colors = PosTheme.colors

    if (!rootState.authResolved) {
        SplashScreen()
        return
    }

    if (!rootState.isAuthenticated) {
        AuthScreen(onAuthenticated = { /* RootViewModel observes auth and re-renders */ })
        return
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(colors.backgroundPrimary)) {
        val isTablet = maxWidth >= 600.dp
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Box(Modifier.weight(1f)) {
                // Quick crossfade between tab content so swaps feel polished rather than
                // jumpy. Keyed by tab + stack-depth so push/pop within a tab also crossfades.
                val stack = navState.stacks[navState.activeTab].orEmpty()
                AnimatedContent(
                    targetState = Triple(navState.activeTab, stack.size, stack.lastOrNull()),
                    transitionSpec = {
                        (fadeIn(PosMotion.tweenStandard()) togetherWith fadeOut(PosMotion.tweenFast()))
                    },
                    label = "tab-content",
                ) { (tab, _, _) ->
                    TabContent(
                        tab = tab,
                        isTablet = isTablet,
                        stack = navState.stacks[tab].orEmpty(),
                        navigator = navigator,
                    )
                }
            }
            TabBar(
                active = navState.activeTab,
                pendingSyncCount = rootState.pendingSyncCount,
                onSelect = navigator::selectTab,
            )
        }
        ModalLayer(modal = navState.modal, navigator = navigator)
    }
}

/**
 * Two synchronized layers for the modal sheet:
 *   1. A fading scrim that dims the screen behind the sheet.
 *   2. The sheet itself, slide-up on enter, slide-down on exit, with a soft fade.
 *
 * The previous implementation wrapped both in a `navState.modal?.let { ... }` block
 * that left composition the moment the modal cleared — so the exit animation never
 * had content to play and the sheet snapped away. Here we drive both layers from a
 * remembered "last seen modal" so the exit animation has stable content, while the
 * `visible` flag is the live `navState.modal != null`.
 *
 * This is the same pattern the catalog uses for the floating cart bar — the data
 * outlives the visibility flag so the animation can complete cleanly.
 */
@Composable
private fun ModalLayer(modal: Destination.Modal?, navigator: Navigator) {
    val visible = modal != null
    // Keep the last non-null modal so the sheet keeps rendering during exit.
    var lastModal by remember { mutableStateOf<Destination.Modal?>(modal) }
    LaunchedEffect(modal) {
        if (modal != null) lastModal = modal
    }
    val rendered = modal ?: lastModal

    // Scrim — fades in/out independently of the sheet so the dim ramps gradually
    // rather than appearing instantly.
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(PosMotion.tweenStandard()),
        exit = fadeOut(PosMotion.tweenStandard()),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(onClick = { navigator.dismissModal() }),
        )
    }

    // Sheet — slides up from the bottom on enter, slides down on exit. The
    // BoxWithConstraints positions it at the bottom of the window.
    val colors = PosTheme.colors
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(PosMotion.tweenStandard()) { it } + fadeIn(PosMotion.tweenStandard()),
            exit = slideOutVertically(PosMotion.tweenStandard()) { it } + fadeOut(PosMotion.tweenFast()),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
                    .clip(PosTheme.shapes.sheet)
                    .background(colors.backgroundPrimary)
                    // Swallow taps on the sheet itself so they don't fall through to
                    // the scrim's dismiss handler.
                    .clickable(enabled = false) {},
            ) {
                rendered?.let { ModalContent(modal = it, navigator = navigator) }
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
                    navigator.selectTab(TabKey.Sell)
                    navigator.popToRoot()
                }
            },
            onReprint = { /* triggered from OrderDetailViewModel */ },
        )
        Destination.Modal.Discount -> DiscountSheet(onDismiss = { navigator.dismissModal() })
        Destination.Modal.SyncStatus -> SyncStatusSheet(onDismiss = { navigator.dismissModal() })
        is Destination.Modal.ProductDetail -> ProductDetailSheet(
            productIdValue = modal.productIdValue,
            onDismiss = { navigator.dismissModal() },
        )
    }
}

// ---- Tab bar ---------------------------------------------------------------

private data class TabSpec(val key: TabKey, val label: String, val glyph: String)

private val tabSpecs = listOf(
    TabSpec(TabKey.Sell, "Shop", "◧"),
    TabSpec(TabKey.Orders, "Orders", "▤"),
    TabSpec(TabKey.Settings, "Account", "◉"),
)

@Composable
private fun TabBar(active: TabKey, pendingSyncCount: Long, onSelect: (TabKey) -> Unit) {
    val colors = PosTheme.colors
    Box(
        Modifier
            .fillMaxWidth()
            .background(colors.backgroundPrimary)
            .border(width = 1.dp, color = colors.separator, shape = androidx.compose.ui.graphics.RectangleShape)
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabSpecs.forEach { spec ->
                TabItem(
                    spec = spec,
                    selected = active == spec.key,
                    badge = if (spec.key == TabKey.Orders) pendingSyncCount.takeIf { it > 0 }?.toString() else null,
                    onClick = { onSelect(spec.key) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun TabItem(
    spec: TabSpec,
    selected: Boolean,
    badge: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PosTheme.colors
    val fg by animateColorAsState(
        targetValue = if (selected) colors.labelPrimary else colors.labelTertiary,
        animationSpec = PosMotion.tweenStandard(),
        label = "tab-fg",
    )
    val pillScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = PosMotion.tweenStandard(),
        label = "pill",
    )
    Column(
        modifier
            .clip(PosTheme.shapes.md)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Soft pill behind the active tab — a sliding indicator without geometry math.
            Box(
                Modifier
                    .size(width = 44.dp, height = 28.dp)
                    .scale(pillScale)
                    .clip(PosTheme.shapes.pill)
                    .background(colors.backgroundSecondary),
            )
            Box {
                Text(spec.glyph, style = PosTheme.typography.title3, color = fg)
                if (badge != null) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 10.dp, y = (-6).dp)
                            .clip(CircleShape)
                            .background(colors.accent)
                            .padding(horizontal = 5.dp, vertical = 1.dp),
                    ) {
                        Text(badge, style = PosTheme.typography.caption2, color = colors.onAccent)
                    }
                }
            }
        }
        Text(spec.label, style = PosTheme.typography.caption2, color = fg)
    }
}

