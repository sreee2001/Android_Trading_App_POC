# Changelog

All notable changes to the Android Trading App POC will be documented in this file.

Format: [Version] - [Date & Time]

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
