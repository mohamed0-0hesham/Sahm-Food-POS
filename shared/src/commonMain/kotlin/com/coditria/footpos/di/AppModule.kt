package com.coditria.footpos.di

import com.coditria.footpos.core.common.DefaultDispatcherProvider
import com.coditria.footpos.core.common.DispatcherProvider
import com.coditria.footpos.core.common.Logger
import com.coditria.footpos.core.common.NapierLogger
import com.coditria.footpos.core.database.DatabaseFactory
import com.coditria.footpos.core.navigation.Navigator
import com.coditria.footpos.data.hardware.MockReceiptPrinter
import com.coditria.footpos.data.repository.InMemoryCartRepository
import com.coditria.footpos.data.repository.OrderRepositoryImpl
import com.coditria.footpos.data.repository.ProductRepositoryImpl
import com.coditria.footpos.data.repository.SyncQueueImpl
import com.coditria.footpos.data.seed.ProductSeeder
import com.coditria.footpos.data.sync.ExponentialBackoffRetryPolicy
import com.coditria.footpos.data.sync.MockPosApi
import com.coditria.footpos.data.sync.RetryPolicy
import com.coditria.footpos.data.sync.SyncWorker
import com.coditria.footpos.domain.hardware.ReceiptPrinter
import com.coditria.footpos.domain.network.PosApi
import com.coditria.footpos.domain.repository.CartRepository
import com.coditria.footpos.domain.repository.OrderRepository
import com.coditria.footpos.domain.repository.ProductRepository
import com.coditria.footpos.domain.repository.SyncQueue
import com.coditria.footpos.domain.usecase.AddItemToCartUseCase
import com.coditria.footpos.domain.usecase.ApplyDiscountUseCase
import com.coditria.footpos.domain.usecase.CalculateOrderTotalUseCase
import com.coditria.footpos.domain.usecase.CancelOrderUseCase
import com.coditria.footpos.domain.usecase.ClearCartUseCase
import com.coditria.footpos.domain.usecase.CompleteOrderUseCase
import com.coditria.footpos.domain.usecase.GetOrderByIdUseCase
import com.coditria.footpos.domain.usecase.ObserveAllSyncUseCase
import com.coditria.footpos.domain.usecase.ObserveCartUseCase
import com.coditria.footpos.domain.usecase.ObserveCategoriesUseCase
import com.coditria.footpos.domain.usecase.ObserveOrderHistoryUseCase
import com.coditria.footpos.domain.usecase.ObservePendingSyncCountUseCase
import com.coditria.footpos.domain.usecase.ObservePendingSyncUseCase
import com.coditria.footpos.domain.usecase.ObserveProductsUseCase
import com.coditria.footpos.domain.usecase.PrintReceiptUseCase
import com.coditria.footpos.domain.usecase.RemoveItemFromCartUseCase
import com.coditria.footpos.domain.usecase.RetryOrderSyncUseCase
import com.coditria.footpos.domain.usecase.RetrySyncOperationUseCase
import com.coditria.footpos.domain.usecase.SearchProductsUseCase
import com.coditria.footpos.domain.usecase.SetOrderNoteUseCase
import com.coditria.footpos.domain.usecase.UpdateItemQuantityUseCase
import com.coditria.footpos.presentation.cart.CartViewModel
import com.coditria.footpos.presentation.catalog.CatalogViewModel
import com.coditria.footpos.presentation.checkout.CheckoutViewModel
import com.coditria.footpos.presentation.orders.OrderDetailViewModel
import com.coditria.footpos.presentation.orders.OrderHistoryViewModel
import com.coditria.footpos.presentation.root.RootViewModel
import com.coditria.footpos.presentation.sync.SyncStatusViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

const val AppScopeQualifier = "AppScope"

val sharedModule = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
    single<Logger> { NapierLogger() }
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single { Navigator() }

    single { DatabaseFactory.create(get()) }

    single<ProductRepository> { ProductRepositoryImpl(get(), get()) }
    single<OrderRepository> { OrderRepositoryImpl(get(), get()) }
    single<CartRepository> { InMemoryCartRepository() }
    single<SyncQueue> { SyncQueueImpl(get(), get()) }
    single { ProductSeeder(get(), get()) }

    single<ReceiptPrinter> { MockReceiptPrinter(get()) }
    single<PosApi> { MockPosApi() }
    single<RetryPolicy> { ExponentialBackoffRetryPolicy() }
    single { SyncWorker(get(), get(), get(), get(), get(), get()) }

    factoryOf(::ObserveProductsUseCase)
    factoryOf(::ObserveCategoriesUseCase)
    factoryOf(::SearchProductsUseCase)
    factoryOf(::ObserveCartUseCase)
    factoryOf(::AddItemToCartUseCase)
    factoryOf(::RemoveItemFromCartUseCase)
    factoryOf(::UpdateItemQuantityUseCase)
    factoryOf(::ApplyDiscountUseCase)
    factoryOf(::SetOrderNoteUseCase)
    factoryOf(::ClearCartUseCase)
    factoryOf(::CalculateOrderTotalUseCase)
    factoryOf(::CompleteOrderUseCase)
    factoryOf(::ObserveOrderHistoryUseCase)
    factoryOf(::GetOrderByIdUseCase)
    factoryOf(::CancelOrderUseCase)
    factoryOf(::RetryOrderSyncUseCase)
    factoryOf(::PrintReceiptUseCase)
    factoryOf(::ObservePendingSyncUseCase)
    factoryOf(::ObserveAllSyncUseCase)
    factoryOf(::ObservePendingSyncCountUseCase)
    factoryOf(::RetrySyncOperationUseCase)

    factory { CatalogViewModel(get(), get(), get(), get(), get()) }
    factory { CartViewModel(get(), get(), get(), get(), get(), get()) }
    factory { CheckoutViewModel(get(), get(), get(), get(), get()) }
    factory { OrderHistoryViewModel(get()) }
    factory { (orderId: com.coditria.footpos.domain.model.OrderId) -> OrderDetailViewModel(orderId, get(), get(), get(), get()) }
    factory { SyncStatusViewModel(get(), get(), get()) }
    factory { RootViewModel(get(), get(), get(), get()) }
}
