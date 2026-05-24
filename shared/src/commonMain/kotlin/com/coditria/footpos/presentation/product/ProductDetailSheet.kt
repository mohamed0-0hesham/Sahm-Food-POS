package com.coditria.footpos.presentation.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.components.PrimaryButton
import com.coditria.footpos.core.designsystem.components.ProductThumbnail
import com.coditria.footpos.core.designsystem.components.QuantityStepper
import com.coditria.footpos.core.designsystem.components.SectionLabel
import com.coditria.footpos.core.designsystem.components.TertiaryButton
import com.coditria.footpos.core.designsystem.shapes
import com.coditria.footpos.core.designsystem.typography
import com.coditria.footpos.domain.model.Product
import com.coditria.footpos.domain.model.ProductId
import com.coditria.footpos.domain.repository.ProductRepository
import com.coditria.footpos.domain.usecase.AddItemToCartUseCase
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ProductDetailSheet(
    productIdValue: String,
    onDismiss: () -> Unit,
    addToCart: AddItemToCartUseCase = koinInject(),
    productRepository: ProductRepository = koinInject(),
) {
    val colors = PosTheme.colors
    val scope = rememberCoroutineScope()
    var product by remember { mutableStateOf<Product?>(null) }
    var quantity by remember { mutableIntStateOf(1) }

    LaunchedEffect(productIdValue) {
        product = productRepository.getById(ProductId(productIdValue))
    }

    val p = product ?: return

    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.backgroundPrimary)
            .padding(PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp)),
    ) {
        // Drag handle — visual cue that the sheet can be dismissed by swipe.
        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp)
                .size(width = 40.dp, height = 4.dp)
                .clip(PosTheme.shapes.pill)
                .background(colors.separator),
        )

        // Hero image — full-width, large, premium feel.
        ProductThumbnail(
            product = p,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 11f)
                .clip(PosTheme.shapes.lg),
        )

        Spacer(Modifier.size(20.dp))

        p.category?.let {
            SectionLabel(text = it)
            Spacer(Modifier.size(6.dp))
        }
        Text(p.name, style = PosTheme.typography.title1, color = colors.labelPrimary)
        p.description?.let {
            Spacer(Modifier.size(8.dp))
            Text(it, style = PosTheme.typography.body, color = colors.labelSecondary)
        }

        Spacer(Modifier.size(20.dp))

        // Price + quantity stacked side-by-side. The stepper sits to the right so
        // the price has more visual weight — what the customer cares about first.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Unit price", style = PosTheme.typography.caption1, color = colors.labelSecondary)
                Spacer(Modifier.height(4.dp))
                Text(
                    p.price.format(),
                    style = PosTheme.typography.title2,
                    color = colors.labelPrimary,
                )
            }
            QuantityStepper(
                value = quantity,
                onDecrement = { if (quantity > 1) quantity-- },
                onIncrement = { quantity++ },
            )
        }

        Spacer(Modifier.size(20.dp))

        // Total summary block. Tonal background so the running total stands apart
        // from the unit price.
        Row(
            Modifier
                .fillMaxWidth()
                .clip(PosTheme.shapes.md)
                .background(colors.backgroundSecondary)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Total", style = PosTheme.typography.body, color = colors.labelSecondary, modifier = Modifier.weight(1f))
            Text(
                (p.price * quantity).format(),
                style = PosTheme.typography.title2,
                color = colors.accent,
            )
        }

        Spacer(Modifier.size(20.dp))

        PrimaryButton(
            text = "Add to order",
            onClick = { scope.launch { addToCart(p, quantity); onDismiss() } },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.size(8.dp))
        TertiaryButton(text = "Cancel", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
    }
}
