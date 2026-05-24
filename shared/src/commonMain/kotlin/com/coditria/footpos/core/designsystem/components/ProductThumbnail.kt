package com.coditria.footpos.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.SubcomposeAsyncImage
import com.coditria.footpos.core.designsystem.PosTheme
import com.coditria.footpos.core.designsystem.typography
import com.coditria.footpos.domain.model.Product

/**
 * Thumbnail with a three-step fallback chain:
 *   1. remote [Product.imageUrl] (loaded by Coil)
 *   2. [Product.emoji] (when bundled / fallback products are showing)
 *   3. first letter of the name
 *
 * Loading + error states slot into the same square so the catalog grid never
 * jumps; the placeholder uses the existing secondary background, matching the
 * sheet look elsewhere in the app.
 */
@Composable
fun ProductThumbnail(
    product: Product,
    modifier: Modifier = Modifier,
) {
    val colors = PosTheme.colors
    val url = product.imageUrl?.takeIf { it.isNotBlank() }

    Box(
        modifier = modifier.background(colors.backgroundSecondary),
        contentAlignment = Alignment.Center,
    ) {
        if (url == null) {
            FallbackGlyph(product)
            return@Box
        }
        SubcomposeAsyncImage(
            model = url,
            contentDescription = product.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = { FallbackGlyph(product) },
            error = { FallbackGlyph(product) },
        )
    }
}

@Composable
private fun FallbackGlyph(product: Product) {
    Text(
        text = product.emoji ?: product.name.take(1).uppercase(),
        style = PosTheme.typography.largeTitle,
    )
}
