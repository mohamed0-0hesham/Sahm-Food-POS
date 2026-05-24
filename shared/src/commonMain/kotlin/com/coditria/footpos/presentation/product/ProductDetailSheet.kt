package com.coditria.footpos.presentation.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.coditria.footpos.core.designsystem.components.TertiaryButton
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

    Column(Modifier.fillMaxWidth().background(colors.backgroundPrimary).padding(20.dp)) {
        ProductThumbnail(
            product = p,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2.5f)
                .clip(RoundedCornerShape(12.dp)),
        )
        Spacer(Modifier.size(16.dp))
        Text(p.name, style = PosTheme.typography.title2, color = colors.labelPrimary)
        p.description?.let {
            Spacer(Modifier.size(4.dp))
            Text(it, style = PosTheme.typography.body, color = colors.labelSecondary)
        }
        p.category?.let {
            Spacer(Modifier.size(8.dp))
            Text("Category: $it", style = PosTheme.typography.caption1, color = colors.labelTertiary)
        }
        Spacer(Modifier.size(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Price", style = PosTheme.typography.title3, color = colors.labelPrimary, modifier = Modifier.weight(1f))
            Text(p.price.format(), style = PosTheme.typography.title2, color = colors.labelPrimary)
        }
        Spacer(Modifier.size(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Quantity", style = PosTheme.typography.body, color = colors.labelPrimary, modifier = Modifier.weight(1f))
            QuantityStepper(value = quantity, onDecrement = { if (quantity > 1) quantity-- }, onIncrement = { quantity++ })
        }
        Spacer(Modifier.size(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Total", style = PosTheme.typography.title3, color = colors.labelPrimary, modifier = Modifier.weight(1f))
            Text((p.price * quantity).format(), style = PosTheme.typography.title2, color = colors.labelPrimary)
        }
        Spacer(Modifier.size(20.dp))
        PrimaryButton(
            text = "Add to Order",
            onClick = { scope.launch { addToCart(p, quantity); onDismiss() } },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.size(8.dp))
        TertiaryButton(text = "Cancel", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
    }
}
