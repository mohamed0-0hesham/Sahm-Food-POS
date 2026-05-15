package com.coditria.footpos.data.seed

import com.coditria.footpos.core.common.DispatcherProvider
import com.coditria.footpos.database.PosDatabase
import com.coditria.footpos.domain.model.Currency
import com.coditria.footpos.domain.model.Money
import com.coditria.footpos.domain.model.Product
import com.coditria.footpos.domain.model.ProductId
import kotlinx.coroutines.withContext

/**
 * Seeds the product catalog on first launch. Idempotent — does nothing if products exist.
 */
class ProductSeeder(
    private val database: PosDatabase,
    private val dispatchers: DispatcherProvider,
) {
    suspend fun seedIfEmpty() = withContext(dispatchers.io) {
        val queries = database.productsQueries
        if (queries.count().executeAsOne() > 0) return@withContext
        sampleProducts.forEach { p ->
            queries.upsert(
                id = p.id.value,
                name = p.name,
                description = p.description,
                priceCents = p.price.amountInCents,
                currency = p.price.currency.name,
                category = p.category,
                barcode = p.barcode,
                imageUrl = p.imageUrl,
                emoji = p.emoji,
            )
        }
    }

    private val sampleProducts: List<Product> = listOf(
        sample("Beef Burger", 8500, "Food", "🍔", "Juicy beef burger with cheese, lettuce, and tomato."),
        sample("Margherita Pizza", 12500, "Food", "🍕", "Classic tomato, mozzarella, and basil."),
        sample("Chicken Shawarma", 7000, "Food", "🌯", "Marinated chicken in pita with garlic sauce."),
        sample("French Fries", 3500, "Food", "🍟", "Crispy golden fries with sea salt."),
        sample("Caesar Salad", 6500, "Food", "🥗", "Romaine, parmesan, croutons, classic dressing."),
        sample("Falafel Wrap", 5500, "Food", "🥙", "Crispy falafel with tahini and pickles."),
        sample("Coca-Cola 330ml", 2500, "Drinks", "🥤", "Chilled soft drink."),
        sample("Fresh Orange Juice", 4000, "Drinks", "🍊"),
        sample("Iced Coffee", 4500, "Drinks", "☕"),
        sample("Mint Lemonade", 3500, "Drinks", "🍋"),
        sample("Mineral Water", 1500, "Drinks", "💧"),
        sample("Chocolate Cake", 5500, "Desserts", "🍰"),
        sample("Vanilla Ice Cream", 3000, "Desserts", "🍦"),
        sample("Cheesecake", 6000, "Desserts", "🧀"),
        sample("Baklava", 4500, "Desserts", "🍪"),
        sample("Mixed Nuts", 4000, "Snacks", "🥜"),
        sample("Potato Chips", 2000, "Snacks", "🥔"),
    )

    private fun sample(
        name: String,
        priceCents: Long,
        category: String,
        emoji: String,
        description: String? = null,
    ) = Product(
        id = ProductId.generate(),
        name = name,
        price = Money(priceCents, Currency.EGP),
        description = description,
        category = category,
        emoji = emoji,
    )
}
