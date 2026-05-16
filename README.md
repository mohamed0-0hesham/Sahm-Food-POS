# Sahm Food POS

A cross-platform Point-of-Sale module for Sahm Food, built with **Kotlin Multiplatform** and **Compose Multiplatform**. Implements the spec in `SAHM_POS_SPEC.md`, the screens in `SAHM_POS_SCREENS_NAVIGATION.md`, and the visual language in `SAHM_POS_DESIGN_BRIEF.md`.

Targets **Android** (tablet-first, phone-adaptive) and **iOS** from one shared codebase.

---

## Architecture

Clean Architecture, organised by package rather than Gradle module — the layering rules are still enforced by direction-of-dependency, and the boundary is obvious from the package tree.

```
shared/src/commonMain/kotlin/com/coditria/footpos/
├── App.kt                       — root composable
├── core/
│   ├── common/                  — Result, AppError, Logger, Uuid, now()
│   ├── database/                — DatabaseDriverFactory (expect/actual), DatabaseFactory
│   ├── designsystem/            — PosTheme, colors, typography, spacing, reusable components
│   └── navigation/              — Navigator (StateFlow-based), Destination sealed hierarchy
├── di/                          — Koin module wiring + KoinInitializer
├── domain/                      — pure Kotlin, zero framework deps
│   ├── model/                   — Money, Order, Product, Receipt, SyncOperation, …
│   ├── repository/              — repo interfaces (ProductRepository, OrderRepository, …)
│   ├── hardware/                — ReceiptPrinter port
│   ├── network/                 — NetworkMonitor, PosApi ports
│   └── usecase/                 — one class per business action
├── data/                        — repository impls, mappers, sync engine, mock adapters
│   ├── repository/              — ProductRepositoryImpl, OrderRepositoryImpl, InMemoryCart, SyncQueueImpl
│   ├── mapper/                  — DB-entity ↔ domain
│   ├── seed/                    — first-launch product seeding
│   ├── sync/                    — SyncWorker, ExponentialBackoffRetryPolicy, MockPosApi
│   └── hardware/                — MockReceiptPrinter
└── presentation/                — Compose screens + MVI ViewModels
    ├── shared/                  — MviViewModel<State, Effect> base class
    ├── splash/  catalog/  cart/  product/  checkout/  receipt/
    ├── orders/                  — history + detail
    ├── settings/                — settings + about
    ├── sync/                    — sync-status sheet
    └── root/                    — RootScreen (tabs + modals)
```

The **dependency rule** points inward at every layer: presentation → domain, data → domain, and domain depends on nothing.

### Why a single Gradle module, not the multi-module split in the spec?

Pragmatism per spec §2: package boundaries enforce the same layering with far less Gradle overhead. The package tree mirrors what the multi-module split would have looked like, so promoting any package to its own module later is mechanical.

---

## Tech stack

| Concern        | Choice                                           |
|----------------|--------------------------------------------------|
| Language       | Kotlin 2.3 (KMP)                                 |
| UI             | Compose Multiplatform 1.11 + Material 3          |
| Architecture   | Clean Architecture + MVI                         |
| Local DB       | SQLDelight 2.0 (Android + Native drivers)        |
| DI             | Koin 4.0 (works in commonMain, ViewModel scope)  |
| Async          | Coroutines + Flow                                |
| Serialization  | kotlinx-serialization                            |
| Time           | kotlin.time + kotlinx-datetime                   |
| Logging        | Napier                                           |
| Navigation     | Hand-rolled `Navigator` (StateFlow stacks)       |
| Tests          | kotlin.test + Turbine + kotlinx-coroutines-test  |

---

## Domain highlights

- **Money is an integer count of cents** (`Money(amountInCents: Long, currency: Currency)`). No floating-point arithmetic anywhere in the order math.
- **Value-class IDs** (`OrderId`, `ProductId`) make accidental cross-wiring of identifiers a compile error.
- **Order computes its own totals** — `subtotal`, `taxAmount`, `total`, `totals` are derived properties, so views and use cases agree on the math by construction.
- **Result<T> / AppError** sealed types replace exceptions across the data and use-case boundary, so the presentation layer pattern-matches on outcomes instead of catching.

---

## Offline-first sync

Local DB is the source of truth (spec §8). Every order goes through:

```
write to DB  →  enqueue SyncOperation  →  return success to UI  (instant)
                       ↓
                SyncWorker (background) sees pending + online
                       ↓
                MockPosApi.syncOrder(order)
                       ↓
        success → removed from queue, order marked SYNCED
        failure → ExponentialBackoffRetryPolicy.nextDelay(retryCount)
                       ↓
                null delay → marked FAILED (manual retry available)
```

The `MockPosApi` simulates ~20% transient failures and per-call latency so the retry path is observable in the running app. The `SyncStatus` sheet (cloud icon, top bar) shows pending and failed operations with a one-tap retry.

---

## Hardware integration

`ReceiptPrinter` is the domain port. `MockReceiptPrinter` implements it:
- Simulates 600–1200 ms hardware latency.
- Fails ~5 % of the time with `AppError.HardwareError("Printer offline")` so the recovery UX is real.
- On success returns the formatted receipt text, which the Receipt sheet renders inside a monospace card. The same text is what `PrintReceiptUseCase` would hand to a real ESC/POS driver.

Adding a real thermal printer is a single new `ReceiptPrinter` implementation; nothing in domain or presentation changes.

---

## Running

```bash
# Android (debug APK)
./gradlew :androidApp:assembleDebug

# Or open the project in Android Studio and run the `androidApp` configuration.
```

```bash
# iOS — open iosApp in Xcode and run the Sahm Food scheme on a simulator.
```

```bash
# Unit tests (20 tests, run on the Android-host JVM)
./gradlew :shared:testAndroidHostTest
```

---

## Tests

- `MoneyTest` — arithmetic, currency mismatch rejection, formatting, no float errors.
- `CalculateOrderTotalUseCaseTest` — tax + discount application across edge cases.
- `ExponentialBackoffRetryPolicyTest` — doubling, max-delay clamp, give-up after N retries.
- `InMemoryCartRepositoryTest` — quantity merge, zero-quantity removal, clear semantics, subtotal aggregation.
- `NavigatorTest` — push/pop, per-tab back stacks, modal-dismissal precedence.

---

## Trade-offs

- **Single Gradle module** — chose package-level Clean Architecture boundaries over multi-module enforcement (faster builds, simpler Gradle, identical dependency rules).
- **Hand-rolled Navigator** — preferred over Voyager because the navigation graph is small (3 tabs + 1 modal slot + per-tab stack), and being able to unit-test the whole router in 40 lines is worth more than a library.
- **`material-icons-extended` isn't published for Compose Multiplatform 1.11** — used Unicode glyphs (`✓ ⚠ ↻ ⌕ ✕ −`) and emoji for tab icons. Apple HIG recommends SF Symbols or a faithful equivalent; Unicode is a clean substitute and ships zero dependencies. Swap to bundled icon assets later if needed.
- **In-memory cart** — drafts are intentionally ephemeral. If draft-recovery becomes a requirement, replace `InMemoryCartRepository` with a DB-backed impl; no caller changes.
- **iOS `NetworkMonitor` is a stub** — always-online (spec explicitly scopes this out). Production would wrap `NWPathMonitor`.
- **Last-Write-Wins conflict resolution** — simplest viable for a POS where orders are append-only. CRDTs / OT would be needed only if multiple terminals can edit the same draft.

---

## Scaling notes

- Promote each `presentation/<feature>/` package to its own Gradle module if build times become a concern.
- Multi-tenancy (branches, multiple registers per branch) → add a `tenantId` value object and thread it through repository keys.
- Real backend → drop `MockPosApi`, wire a Ktor `HttpClient` behind the same `PosApi` interface. Sync worker, retry policy, and queue stay unchanged.
- Real printer → new `ReceiptPrinter` actual on the Android side via Bluetooth/USB; iOS via ExternalAccessory.
