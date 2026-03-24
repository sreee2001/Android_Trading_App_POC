# Changelog

All notable changes to the Android Trading App POC will be documented in this file.

Format: [Version] - [Date & Time]

---

## [0.2.0] - 2026-03-24 13:00 UTC

### Added

**Price Flash Animations**

- Created `PriceFlash.kt` composable component implementing real-time price tick flash animation — prices briefly flash green on uptick and red on downtick, then fade back, mimicking production trading terminal behavior
- Added `rememberTickDirection` composable that tracks previous price and computes `TickDirection` (UP, DOWN, NONE) on each update
- Added `FlashingPriceText` composable with animated background flash and text color transition using `animateColorAsState` with configurable timing (100ms flash-in, 500ms fade-out)
- Integrated `FlashingPriceText` into `StockTickerCard` — top movers list now flashes on every price update
- Integrated `FlashingPriceText` into `WatchlistScreen` `WatchlistItemRow` — watchlist prices flash on tick
- Integrated `FlashingPriceText` into `DashboardScreen` `MarketIndexCard` — S&P 500, NASDAQ, DOW index values flash on update
- Integrated `FlashingPriceText` into `StockDetailScreen` `PriceHeader` — main stock detail price header flashes on tick
- Integrated `FlashingPriceText` into `PortfolioScreen` `PositionCard` — portfolio position market values flash on price change

**Finnhub Real API Integration**

- Created `FinnhubModels.kt` with complete API response models: `FinnhubTradeMessage`, `FinnhubTrade`, `FinnhubQuoteResponse`, `FinnhubCandleResponse`, `FinnhubCompanyProfile`, `FinnhubSearchResponse`, `FinnhubSearchResult` — all with Gson `@SerializedName` annotations matching Finnhub's single-letter JSON keys
- Created `FinnhubApi.kt` Retrofit service interface with endpoints for quote, candles, company profile, and symbol search
- Created `FinnhubWebSocket.kt` implementing real-time trade streaming via OkHttp WebSocket to `wss://ws.finnhub.io`, with automatic symbol subscription/unsubscription, JSON parsing of trade messages, and proper cleanup on Flow cancellation using `callbackFlow`
- Created `FinnhubDataSource.kt` that combines REST polling and WebSocket streaming, maps Finnhub responses to domain models, caches company profiles, handles interval-to-resolution mapping, and computes appropriate time ranges for candlestick requests
- Created `NetworkModule.kt` Hilt DI module providing `OkHttpClient` (with auth interceptor injecting API token, logging interceptor, and WebSocket keep-alive ping interval), `Retrofit` instance, `FinnhubApi` service, and `FinnhubWebSocket` — all as singletons
- Created `DataSourceConfig.kt` with `DataSourceMode` enum (SIMULATED, LIVE, HYBRID) implementing the Strategy Pattern for data source selection, defaulting to HYBRID mode

**Strategy Pattern in MarketDataRepository**

- Rewrote `MarketDataRepositoryImpl` to accept both `MarketSimulator` and `FinnhubDataSource` as dependencies
- Implemented three-way routing based on `DataSourceConfig.mode`: SIMULATED routes entirely to local simulator, LIVE routes to Finnhub, HYBRID tries Finnhub first and falls back to simulator on any error
- Added graceful fallback logic using Kotlin Flow `catch` operator — ensures the app always shows data even when network fails (critical for trading UIs)
- Order book, market indices, sector performance, and top movers always use simulator (Finnhub free tier doesn't provide these)

**Unit Test Suite (123 tests)**

- Created `StockQuoteTest` (5 tests) — isPositive, spread calculation, timestamp default
- Created `PositionTest` (9 tests) — marketValue, costBasis, unrealizedPnL, unrealizedPnLPercent, isProfit for profit/loss/break-even/zero-cost scenarios
- Created `PortfolioTest` (6 tests) — totalMarketValue, totalCostBasis, totalUnrealizedPnL, totalValue, buyingPower, empty portfolio
- Created `OrderTest` (12 tests) — remainingQuantity, isFilled for all statuses, isActive for all 7 order statuses, enum completeness for OrderType and TimeInForce
- Created `CandlestickTest` (8 tests) — isBullish, body, upperWick, lowerWick, doji candle
- Created `OrderBookTest` (5 tests) — spread calculation with/without bids/asks, spreadPercent
- Created `MarketDataModelTest` (7 tests) — MarketIndex.isPositive, SectorPerformance.isPositive, WatchlistItem.isPositive
- Created `MarketSimulatorTest` (33 tests) — symbol catalog, quote generation (valid, changing prices, high>=low, bid<ask), streaming (quote, quotes, indices, sectors), candlestick generation (counts, timestamps sequential), order book (valid structure, bid/ask sorting), search (symbol, name, case-insensitive, empty), order execution (market buy, cash reduction, sell without position, buy+sell roundtrip, exceeding buying power, zero/negative quantity, unknown symbol, order history, position accumulation), portfolio (initial cash, empty portfolio, after trades), top movers sorting
- Created `FormattersTest` (18 tests) — formatPrice, formatChange, formatPercent, formatVolume (millions/billions/thousands/small), formatMarketCap (trillions/billions/millions), formatCurrency, formatTimestamp
- Created `OrderEntryUiStateTest` (8 tests) — estimatedCost for market/limit/stop/stop-limit/trailing-stop orders, empty/non-numeric quantity, limit price fallback
- Created `DataSourceConfigTest` (4 tests) — default mode, mode switching, all modes defined
- Created `FinnhubDataSourceMappingTest` (8 tests) — interval-to-resolution mapping for all 7 intervals plus fallback
- Added `coroutines-test` and `turbine` test dependencies to version catalog and app build file for Flow testing

### Changed

- `MarketDataRepositoryImpl` now depends on both `MarketSimulator` and `FinnhubDataSource` instead of simulator only — implements Strategy Pattern with hybrid fallback
- `StockTickerCard` price display now uses `FlashingPriceText` instead of static `Text`
- `WatchlistItemRow` price display now uses `FlashingPriceText` instead of static `Text`
- `MarketIndexCard` value display now uses `FlashingPriceText` instead of static `Text`
- `StockDetailScreen` `PriceHeader` now uses `FlashingPriceText` instead of static `Text`
- `PortfolioScreen` `PositionCard` market value display now uses `FlashingPriceText` instead of static `Text`
- `libs.versions.toml` — added `turbine = "1.1.0"`, `coroutines-test` and `turbine` library aliases
- `app/build.gradle.kts` — added `testImplementation` entries for `coroutines-test` and `turbine`

### Files Changed

**New Files (19)**

| File | Purpose |
|------|---------|
| `presentation/components/PriceFlash.kt` | `TickDirection` enum, `rememberTickDirection`, `FlashingPriceText` composable |
| `data/remote/model/FinnhubModels.kt` | API response DTOs with `@SerializedName` for Finnhub's single-letter keys |
| `data/remote/FinnhubApi.kt` | Retrofit service — quote, candles, company profile, symbol search |
| `data/remote/FinnhubWebSocket.kt` | OkHttp WebSocket `callbackFlow` streaming Finnhub trades in real time |
| `data/remote/FinnhubDataSource.kt` | Maps Finnhub REST + WebSocket to domain models, caches profiles |
| `di/NetworkModule.kt` | Hilt module — OkHttpClient, Retrofit, FinnhubApi, FinnhubWebSocket singletons |
| `data/DataSourceConfig.kt` | `DataSourceMode` enum (SIMULATED / LIVE / HYBRID), Strategy Pattern config |
| `test/.../StockQuoteTest.kt` | 5 unit tests — isPositive, spread, timestamp |
| `test/.../PositionTest.kt` | 9 unit tests — marketValue, costBasis, PnL, PnLPercent, isProfit |
| `test/.../PortfolioTest.kt` | 6 unit tests — totals, empty portfolio |
| `test/.../OrderTest.kt` | 12 unit tests — remaining qty, isFilled, isActive, enum completeness |
| `test/.../CandlestickTest.kt` | 8 unit tests — bullish/bearish, body, wicks, doji |
| `test/.../OrderBookTest.kt` | 5 unit tests — spread, spreadPercent |
| `test/.../MarketDataModelTest.kt` | 7 unit tests — index/sector/watchlist isPositive |
| `test/.../MarketSimulatorTest.kt` | 33 unit tests — quotes, streaming, candles, orderbook, search, orders, portfolio |
| `test/.../FormattersTest.kt` | 18 unit tests — all formatting functions |
| `test/.../OrderEntryUiStateTest.kt` | 8 unit tests — estimatedCost for all order types |
| `test/.../DataSourceConfigTest.kt` | 4 unit tests — default mode, switching, enum completeness |
| `test/.../FinnhubDataSourceMappingTest.kt` | 8 unit tests — interval-to-resolution mapping |

**Modified Files (7)**

| File | Change |
|------|--------|
| `data/repository/MarketDataRepositoryImpl.kt` | Rewritten — now injects both Simulator & FinnhubDataSource, three-way routing with hybrid fallback |
| `presentation/components/StockTickerCard.kt` | Price `Text` → `FlashingPriceText` |
| `presentation/watchlist/WatchlistScreen.kt` | Price `Text` → `FlashingPriceText` |
| `presentation/dashboard/DashboardScreen.kt` | Index value `Text` → `FlashingPriceText` |
| `presentation/stockdetail/StockDetailScreen.kt` | Price header `Text` → `FlashingPriceText` |
| `presentation/portfolio/PortfolioScreen.kt` | Position value `Text` → `FlashingPriceText` |
| `gradle/libs.versions.toml` | Added turbine, coroutines-test entries |
| `app/build.gradle.kts` | Added test dependencies |

### Build & Test Results

- `gradlew assembleDebug` — **BUILD SUCCESSFUL**
- `gradlew testDebugUnitTest` — **123 tests, 0 failures, 0 errors** across 12 test suites
- APK installed on emulator (Medium_Phone_API_36) — **zero crashes verified via logcat**

### Removed

- Nothing

---

## [0.1.1] - 2026-03-24 11:30 UTC

### Added

- Created comprehensive `.gitignore` file covering Android build outputs, Gradle caches, IDE configuration files, local properties, OS-generated artifacts, signing keystores, and test screenshots

---

## [0.1.0] - 2026-03-24 10:00 UTC

### Added

**Project Setup**

- Initialized Android project with Kotlin DSL Gradle build system
- Configured Gradle version catalog (`libs.versions.toml`) with all dependency versions centralized
- Configured root `build.gradle.kts` and app-level `build.gradle.kts` with Compose, Hilt, Room, and Retrofit plugins
- Set up `gradle.properties` with AndroidX, non-transitive R classes, and Kotlin code generation
- Created `settings.gradle.kts` with Google, Maven Central, and Gradle Plugin Portal repositories
- Created `AndroidManifest.xml` with internet permission, Hilt application class, and Material 3 theme reference

**Domain Layer — Models**

- Created `StockQuote` data class representing a live equity quote with symbol, name, price, change, percent change, volume, and timestamp
- Created `Candlestick` data class with open, high, low, close, volume, and timestamp fields for OHLC charting
- Created `Order` data class with full order semantics including order type enum (Market, Limit, Stop, Stop-Limit, Trailing Stop), side enum (Buy, Sell), time-in-force enum (DAY, GTC, IOC, FOK), and order status enum modeling the complete order lifecycle (New, Acknowledged, PartiallyFilled, Filled, Cancelled, Rejected)
- Created `Portfolio` data class containing cash balance, total value, day P&L, total P&L, and a list of positions
- Created `Position` data class with symbol, quantity, average cost, current price, market value, unrealized P&L, and percent return fields
- Created `WatchlistItem` data class with symbol, name, price, change, percent change, and sparkline data list
- Created `OrderBook` data class with bid levels, ask levels, spread, and spread percentage
- Created `OrderBookLevel` data class with price, size, and cumulative size for depth representation
- Created `MarketIndex` data class for major indices display with name, value, change, and percent change
- Created `SectorPerformance` data class for heatmap rendering with sector name and percent change

**Domain Layer — Repository Interfaces**

- Created `MarketDataRepository` interface defining contracts for streaming quotes, fetching candlestick data, retrieving order book, market indices, sector performance, top movers, and symbol search
- Created `OrderRepository` interface defining contracts for placing orders, cancelling orders, streaming order updates, and fetching order history
- Created `PortfolioRepository` interface defining contracts for streaming portfolio updates and retrieving portfolio snapshots
- Created `WatchlistRepository` interface defining contracts for streaming watchlist, adding symbols, removing symbols, and reordering items

**Data Layer — Market Simulator**

- Built complete `MarketSimulator` class that generates realistic market data without any external API dependency
- Implemented random walk with drift price generation for 20 pre-seeded stock symbols across multiple sectors (AAPL, GOOGL, MSFT, AMZN, TSLA, META, NVDA, JPM, BAC, GS, JNJ, PFE, UNH, XOM, CVX, DIS, NFLX, SPOT, AMD, INTC)
- Implemented simulated candlestick history generation producing 100 candles per symbol with realistic OHLC relationships
- Implemented order book simulation with 10 bid levels and 10 ask levels generated around current price with randomized sizes
- Implemented market index simulation for S&P 500, NASDAQ, and DOW JONES with correlated movement
- Implemented sector performance simulation for Technology, Healthcare, Finance, Energy, Consumer, and Communication sectors
- Implemented order execution engine that validates orders against buying power and position availability, generates fills with simulated slippage, and transitions order state correctly
- Implemented portfolio management with virtual cash account starting at $100,000, position tracking with average cost calculation, and P&L computation
- Implemented top movers calculation sorting stocks by absolute percent change
- Implemented symbol search with case-insensitive matching against symbol and company name
- All streaming functions emit updates via Kotlin Flows on configurable intervals

**Data Layer — Repository Implementations**

- Created `MarketDataRepositoryImpl` delegating all calls to MarketSimulator and exposing results as Flows
- Created `OrderRepositoryImpl` delegating order placement, cancellation, and history to MarketSimulator
- Created `PortfolioRepositoryImpl` delegating portfolio streaming and snapshots to MarketSimulator
- Created `WatchlistRepositoryImpl` with in-memory watchlist state management, supporting add, remove, reorder, and continuous price update streaming via MarketSimulator

**Dependency Injection**

- Created Hilt `AppModule` with singleton bindings for MarketSimulator and all four repository interfaces to their implementations

**Application Entry Point**

- Created `TradingApplication` class annotated with `@HiltAndroidApp` for Hilt dependency injection initialization
- Created `MainActivity` with `@AndroidEntryPoint` annotation, edge-to-edge display configuration, and Compose content hosting

**Presentation — Theme**

- Designed complete dark trading theme color palette with 30+ color tokens including trading green, trading red, chart colors, background hierarchy, surface variants, and text hierarchy
- Created custom `TradingTypography` with Inter font family covering display, headline, title, body, and label styles
- Built Material 3 dark color scheme mapped to trading-specific colors
- Created `TradingTheme` composable wrapping Material 3 theming with status bar and navigation bar color configuration

**Presentation — Reusable Components**

- Built `CandlestickChart` composable with full Canvas-drawn rendering of OHLC candles including green bullish and red bearish coloring, wick lines, body rectangles, time axis labels, price axis labels, and crosshair overlay on long press
- Built `SparklineChart` composable rendering a mini line chart with gradient fill beneath the line, used in watchlist rows for at-a-glance price trend visualization
- Built `DepthChart` composable showing cumulative bid and ask volume as filled area paths with green bids and red asks
- Built `OrderBookView` composable displaying bid and ask ladders with horizontal volume bars, color-coded rows, and spread indicator in the center
- Built `StockTickerCard` composable for watchlist rows showing symbol, company name, sparkline, price, and color-coded change with arrow indicators
- Built `PortfolioSummaryCard` composable displaying total portfolio value, day P&L, total P&L, and buying power in a gradient-bordered card
- Built `PriceChangeIndicator` composable showing price change and percent change with directional arrow icons and green/red coloring
- Created `Formatters` utility object with functions for currency formatting, percent formatting, large number abbreviation, and volume formatting

**Presentation — Dashboard Screen**

- Created `DashboardViewModel` managing UI state for market indices, sector performance, and top movers with automatic streaming on initialization
- Created `DashboardScreen` composable with three sections: market indices row showing S&P 500, NASDAQ, and DOW as cards with live values; sector heatmap as a grid of color-coded sector performance tiles; and top movers list showing most active stocks with price and change indicators

**Presentation — Watchlist Screen**

- Created `WatchlistViewModel` managing watchlist state, search functionality with debounced query, and add/remove operations
- Created `WatchlistScreen` composable with search bar for finding and adding symbols, real-time streaming watchlist with sparkline cards, swipe-to-delete on each item, and empty state display when no symbols are being watched

**Presentation — Stock Detail Screen**

- Created `StockDetailViewModel` accepting symbol as a saved state handle parameter, streaming live quote, candlestick data, and order book for the selected symbol
- Created `StockDetailScreen` composable with a comprehensive layout: stock name and live price header with change indicator, interactive candlestick chart occupying the top portion, order book bid/ask ladder in the middle, depth chart showing cumulative volume, key statistics row (open, high, low, volume), and a fixed "Trade" button at the bottom navigating to order entry

**Presentation — Order Entry Screen**

- Created `OrderEntryViewModel` managing order form state including side toggling, order type selection, quantity input, price input, stop price input, time-in-force selection, and form validation logic checking buying power, position availability, and price validity
- Created `OrderEntryScreen` composable with buy/sell toggle buttons, order type dropdown (Market, Limit, Stop, Stop-Limit, Trailing Stop), quantity input with quick-select percentage buttons (25%, 50%, 75%, 100%), conditional price and stop price fields based on order type, time-in-force selector, order summary card showing estimated cost, and confirmation dialog before submission with success feedback

**Presentation — Portfolio Screen**

- Created `PortfolioViewModel` managing real-time portfolio state and order history streaming
- Created `PortfolioScreen` composable with two tabs: Positions tab showing portfolio summary card and a list of individual holdings with symbol, quantity, average cost, current price, market value, unrealized P&L, and percent return; and Orders tab showing complete order history with order type, side, status, quantity, and fill information with color-coded status badges

**Presentation — Navigation**

- Created `TradingNavGraph` defining five routes: Dashboard, Watchlist, Portfolio, Stock Detail (parameterized by symbol), and Order Entry (parameterized by symbol)
- Implemented bottom navigation bar with three tabs (Markets, Watchlist, Portfolio) using Material 3 NavigationBar with trading-themed colors
- Implemented Scaffold-based main layout with navigation bar and Compose Navigation host

---
