package com.coditria.footpos.di

import com.coditria.footpos.core.common.DefaultDispatcherProvider
import com.coditria.footpos.core.common.DispatcherProvider
import com.coditria.footpos.core.common.Logger
import com.coditria.footpos.core.common.NapierLogger
import com.coditria.footpos.core.database.DatabaseFactory
import com.coditria.footpos.core.navigation.Navigator
import com.coditria.footpos.core.network.buildHttpClient
import com.coditria.footpos.core.network.httpEngineFactory
import com.coditria.footpos.data.auth.AuthDataSource
import com.coditria.footpos.data.auth.DummyAuthDataSource
import com.coditria.footpos.data.hardware.MockReceiptPrinter
import com.coditria.footpos.data.product.local.ProductLocalDataSource
import com.coditria.footpos.data.product.local.SqlDelightProductLocalDataSource
import com.coditria.footpos.data.product.remote.DummyJsonRemoteProductDataSource
import com.coditria.footpos.data.product.remote.ProductRemoteDataSource
import com.coditria.footpos.data.repository.AuthRepositoryImpl
import com.coditria.footpos.data.repository.InMemoryCartRepository
import com.coditria.footpos.data.repository.OrderRepositoryImpl
import com.coditria.footpos.data.repository.ProductRepositoryImpl
import com.coditria.footpos.data.repository.SettingsRepositoryImpl
import com.coditria.footpos.data.repository.SyncQueueImpl
import com.coditria.footpos.data.sync.ExponentialBackoffRetryPolicy
import com.coditria.footpos.data.sync.MockPosApi
import com.coditria.footpos.data.sync.RetryPolicy
import com.coditria.footpos.data.sync.SyncWorker
import com.coditria.footpos.domain.hardware.ReceiptPrinter
import com.coditria.footpos.domain.network.PosApi
import com.coditria.footpos.domain.repository.AuthRepository
import com.coditria.footpos.domain.repository.CartRepository
import com.coditria.footpos.domain.repository.OrderRepository
import com.coditria.footpos.domain.repository.ProductRepository
import com.coditria.footpos.domain.repository.SettingsRepository
import com.coditria.footpos.domain.repository.SyncQueue
import com.coditria.footpos.domain.usecase.AddItemToCartUseCase
import com.coditria.footpos.domain.usecase.ApplyDiscountUseCase
import com.coditria.footpos.domain.usecase.CalculateOrderTotalUseCase
import com.coditria.footpos.domain.usecase.CancelOrderUseCase
import com.coditria.footpos.domain.usecase.ClearCartUseCase
import com.coditria.footpos.domain.usecase.CompleteOrderUseCase
import com.coditria.footpos.domain.usecase.GetOrderByIdUseCase
import com.coditria.footpos.domain.usecase.GetSettingsUseCase
import com.coditria.footpos.domain.usecase.ObserveTodayStatsUseCase
import com.coditria.footpos.domain.usecase.ObserveTopSellersUseCase
import com.coditria.footpos.domain.usecase.ObserveAllSyncUseCase
import com.coditria.footpos.domain.usecase.ObserveCartUseCase
import com.coditria.footpos.domain.usecase.ObserveCategoriesUseCase
import com.coditria.footpos.domain.usecase.ObserveOrderHistoryUseCase
import com.coditria.footpos.domain.usecase.ObservePendingSyncCountUseCase
import com.coditria.footpos.domain.usecase.ObservePendingSyncUseCase
import com.coditria.footpos.domain.usecase.ObserveProductsUseCase
import com.coditria.footpos.domain.usecase.ObserveSettingsUseCase
import com.coditria.footpos.domain.usecase.PrintReceiptUseCase
import com.coditria.footpos.domain.usecase.RefreshProductsUseCase
import com.coditria.footpos.domain.usecase.RemoveItemFromCartUseCase
import com.coditria.footpos.domain.usecase.RetryOrderSyncUseCase
import com.coditria.footpos.domain.usecase.RetrySyncOperationUseCase
import com.coditria.footpos.domain.usecase.SearchProductsUseCase
import com.coditria.footpos.domain.usecase.SetOrderNoteUseCase
import com.coditria.footpos.domain.usecase.UpdateAutoPrintUseCase
import com.coditria.footpos.domain.usecase.UpdateCurrencyUseCase
import com.coditria.footpos.domain.usecase.UpdateItemQuantityUseCase
import com.coditria.footpos.domain.usecase.UpdateStoreNameUseCase
import com.coditria.footpos.domain.usecase.UpdateTaxRateUseCase
import com.coditria.footpos.domain.usecase.GetCurrentUserUseCase
import com.coditria.footpos.domain.usecase.ObserveCurrentUserUseCase
import com.coditria.footpos.domain.usecase.SignInWithAppleUseCase
import com.coditria.footpos.domain.usecase.SignInWithEmailUseCase
import com.coditria.footpos.domain.usecase.SignInWithGoogleUseCase
import com.coditria.footpos.domain.usecase.SignOutUseCase
import com.coditria.footpos.domain.usecase.SignUpWithEmailUseCase
import com.coditria.footpos.presentation.auth.AuthViewModel
import com.coditria.footpos.presentation.cart.CartViewModel
import com.coditria.footpos.presentation.catalog.CatalogViewModel
import com.coditria.footpos.presentation.checkout.CheckoutViewModel
import com.coditria.footpos.presentation.orders.OrderDetailViewModel
import com.coditria.footpos.presentation.orders.OrderHistoryViewModel
import com.coditria.footpos.presentation.root.RootViewModel
import com.coditria.footpos.presentation.settings.SettingsViewModel
import com.coditria.footpos.presentation.sync.SyncStatusViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val sharedModule = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
    single<Logger> { NapierLogger() }
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single { Navigator() }

    single { DatabaseFactory.create(get()) }

    // Multiplatform-settings: SettingsFactory.create() returns the platform-appropriate
    // Settings (SharedPreferences on Android, NSUserDefaults on iOS).
    single { get<com.coditria.footpos.data.settings.SettingsFactory>().create() }

    single { buildHttpClient(httpEngineFactory(), get()) }

    single<ProductLocalDataSource> { SqlDelightProductLocalDataSource(get(), get()) }
    single<ProductRemoteDataSource> { DummyJsonRemoteProductDataSource(get(), get()) }
    single<ProductRepository> { ProductRepositoryImpl(get(), get()) }
    single<OrderRepository> { OrderRepositoryImpl(get(), get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    // Cart needs the settings stream (live tax rate) and the app scope to subscribe.
    single<CartRepository> { InMemoryCartRepository(get(), get()) }
    single<SyncQueue> { SyncQueueImpl(get(), get()) }
    single<AuthDataSource> { DummyAuthDataSource(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get()) }

    single<ReceiptPrinter> { MockReceiptPrinter(get()) }
    single<PosApi> { MockPosApi() }
    single<RetryPolicy> { ExponentialBackoffRetryPolicy() }
    single { SyncWorker(get(), get(), get(), get(), get(), get()) }

    factoryOf(::ObserveProductsUseCase)
    factoryOf(::ObserveCategoriesUseCase)
    factoryOf(::SearchProductsUseCase)
    factoryOf(::RefreshProductsUseCase)
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
    factoryOf(::ObserveSettingsUseCase)
    factoryOf(::GetSettingsUseCase)
    factoryOf(::UpdateStoreNameUseCase)
    factoryOf(::UpdateTaxRateUseCase)
    factoryOf(::UpdateCurrencyUseCase)
    factoryOf(::UpdateAutoPrintUseCase)
    factoryOf(::ObserveCurrentUserUseCase)
    factoryOf(::GetCurrentUserUseCase)
    factoryOf(::SignInWithEmailUseCase)
    factoryOf(::SignUpWithEmailUseCase)
    factoryOf(::SignInWithGoogleUseCase)
    factoryOf(::SignInWithAppleUseCase)
    factoryOf(::SignOutUseCase)
    factoryOf(::ObserveTopSellersUseCase)
    factoryOf(::ObserveTodayStatsUseCase)

    factory { CatalogViewModel(get(), get(), get(), get(), get(), get(), get()) }
    factory { CartViewModel(get(), get(), get(), get(), get(), get(), get()) }
    factory { CheckoutViewModel(get(), get(), get(), get(), get()) }
    factory { OrderHistoryViewModel(get()) }
    factory { (orderId: com.coditria.footpos.domain.model.OrderId) -> OrderDetailViewModel(orderId, get(), get(), get(), get()) }
    factory { SyncStatusViewModel(get(), get(), get()) }
    factory { SettingsViewModel(get(), get(), get(), get(), get(), get()) }
    factory { RootViewModel(get(), get(), get(), get(), get()) }
    factory { AuthViewModel(get(), get(), get(), get(), get(), get()) }
}
