# EventPro Admin

[![CI](https://github.com/pallab-js/eventpro/actions/workflows/ci.yml/badge.svg)](https://github.com/pallab-js/eventpro/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![API](https://img.shields.io/badge/API-26%2B-brightgreen)](https://developer.android.com/about/versions/oreo)

An offline-first Android app for event management professionals. Track events, clients, inventory, and finances — all on-device with no internet required.

## Features

- **Executive Dashboard** — Revenue snapshot (trailing 30d), pending/overdue alerts, critical milestones, daily agenda timeline
- **Event Pipeline** — Full lifecycle: Draft → Confirmed → In Progress → Completed. Filter by status or time range. Budget tracking per event.
- **Client CRM** — Contact directory with search, status filters, and event history per client
- **Inventory Management** — Stock tracking with availability status (In Stock / Low Stock / Out of Stock), category filtering
- **Financial Ledger** — Revenue vs. expense tracking, pending invoices, expense breakdown by category with visual chart
- **100% Offline** — All data stored locally via Room SQLite. No accounts, no servers, no internet dependency.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose, Material3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Database | Room SQLite |
| Async | Kotlin Coroutines & Flow |
| Charts | Vico |
| Build | Gradle KTS, Kotlin DSL |

## Requirements

- Android 8.0+ (API 26)
- Gradle 8.x
- JDK 17
- Android Studio Ladybug+ (recommended)

## Build & Run

```bash
# Build debug APK
./gradlew assembleDebug

# Install on device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Run unit tests
./gradlew test
```

## Project Structure

```
app/
├── src/main/java/com/eventpro/admin/
│   ├── data/local/          # Room DB, DAOs, entities, converters
│   ├── di/                  # Hilt modules
│   ├── domain/
│   │   ├── model/           # Domain models
│   │   └── repository/      # Repository interfaces
│   ├── repository/          # Repository implementations
│   ├── ui/
│   │   ├── components/      # Shared composables
│   │   ├── dashboard/       # Dashboard screen + VM
│   │   ├── events/          # Event list/detail/edit screens + VMs
│   │   ├── clients/         # Client screens + VM
│   │   ├── inventory/       # Inventory screens + VM
│   │   ├── ledger/          # Ledger screens + VMs
│   │   └── navigation/      # Nav host & bottom bar
│   └── util/                # Formatters, extensions
└── src/test/                # Unit tests
```

## Architecture

MVVM + Clean Architecture with unidirectional data flow:

```
UI (Compose) → ViewModel → Repository → Room DAO → SQLite
     ↑            |             |
     └──── State ←┘ ←──────────┘
```

- Screens observe `StateFlow` from ViewModels
- ViewModels scope coroutines via `viewModelScope`
- Repositories abstract data sources behind interfaces
- Room DAOs return `Flow<List<T>>` for reactive updates

## CI

GitHub Actions runs `./gradlew test assembleDebug` on every push.

## License

Apache 2.0 — see [LICENSE](LICENSE).
