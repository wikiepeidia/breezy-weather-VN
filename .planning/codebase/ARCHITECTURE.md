# Architecture

<!-- refreshed: 2026-05-13 -->
**Analysis Date:** 2026-05-13

## Pattern

**Clean Architecture + MVVM** with a Source plugin system.

- Domain models defined in the `:domain` module (pure Kotlin, no Android deps).
- Data persistence via SQLDelight in the `:data` module.
- 50+ weather/geocoding sources implemented as injectable plugins inside `:app`.
- UI built with a mix of traditional Android Views (RecyclerView + ViewHolder) for the main weather screen and Jetpack Compose for settings, alert, and detail screens.
- Dependency Injection via Dagger Hilt throughout.
- Network calls use RxJava 3 at the Source boundary; everything else uses Kotlin Coroutines.

---

## Layer Overview

```
┌────────────────────────────────────────────────────────────────┐
│                       :app module                               │
│                                                                 │
│  Activities / Fragments / ViewModels (MVVM)                     │
│  app/src/main/kotlin/org/breezyweather/ui/                      │
│                                                                 │
│  ┌─────────────┐  ┌──────────────────┐  ┌──────────────────┐  │
│  │  ui/main/   │  │ ui/settings/     │  │ ui/alert/        │  │
│  │  (Views +   │  │ (Compose Screens)│  │ ui/details/      │  │
│  │  Fragments) │  └──────────────────┘  │ (Compose Screens)│  │
│  └─────────────┘                        └──────────────────┘  │
│                                                                 │
│  Source Plugin Layer                                            │
│  app/src/main/kotlin/org/breezyweather/sources/                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  SourceManager  ←  50+ Source implementations           │  │
│  │  RefreshHelper  (orchestrates weather refresh)           │  │
│  └──────────────────────────────────────────────────────────┘  │
├────────────────────────────────────────────────────────────────┤
│                       :data module                              │
│  data/src/main/kotlin/breezyweather/data/                       │
│  LocationRepository  │  WeatherRepository  │  DatabaseHandler  │
│  SQLDelight-generated queries (sqldelight/)                     │
├────────────────────────────────────────────────────────────────┤
│                       :domain module                            │
│  domain/src/main/kotlin/breezyweather/domain/                   │
│  Location model  │  Weather model  │  SourceFeature enum       │
├────────────────────────────────────────────────────────────────┤
│  :weather-unit   │  :maps-utils   │  :ui-weather-view           │
│  Unit types &    │  Google Maps   │  Material animated          │
│  conversions     │  geometry lib  │  weather background view    │
└────────────────────────────────────────────────────────────────┘
         │                  │
         ▼                  ▼
  SQLDelight DB       External APIs (OkHttp + Retrofit + RxJava 3)
```

---

## Component Responsibilities

| Component | Responsibility | Location |
|-----------|----------------|----------|
| `BreezyActivity` | Base Activity: edge-to-edge, theme, lifecycle | `app/src/main/kotlin/org/breezyweather/common/activities/BreezyActivity.kt` |
| `BreezyViewModel` | Base ViewModel: new-instance guard | `app/src/main/kotlin/org/breezyweather/common/activities/BreezyViewModel.kt` |
| `MainActivity` | Entry point: Fragment host, window insets, splash | `app/src/main/kotlin/org/breezyweather/ui/main/MainActivity.kt` |
| `MainActivityViewModel` | State owner for current location list, weather data, refresh | `app/src/main/kotlin/org/breezyweather/ui/main/MainActivityViewModel.kt` |
| `SourceManager` | Registry: injects and aggregates all Source instances | `app/src/main/kotlin/org/breezyweather/sources/SourceManager.kt` |
| `RefreshHelper` | Orchestrates source calls, merges results, persists data | `app/src/main/kotlin/org/breezyweather/sources/RefreshHelper.kt` |
| `LocationRepository` | CRUD for Location entities via SQLDelight | `data/src/main/kotlin/breezyweather/data/location/LocationRepository.kt` |
| `WeatherRepository` | CRUD for Weather entities via SQLDelight | `data/src/main/kotlin/breezyweather/data/weather/WeatherRepository.kt` |
| `DatabaseHandler` | SQLDelight coroutine executor wrapper | `data/src/main/kotlin/breezyweather/data/DatabaseHandler.kt` |
| `WeatherUpdateJob` | Background scheduled weather refresh (WorkManager) | `app/src/main/kotlin/org/breezyweather/background/weather/WeatherUpdateJob.kt` |
| `NominatimService` | Unified Nominatim + LocationIQ geocoding/search source | `app/src/main/kotlin/org/breezyweather/sources/nominatim/NominatimService.kt` |

---

## Key Patterns

### Source Plugin Pattern

Every external data provider implements one or more `Source` sub-interfaces:

- `Source` — base: `id`, `name`, grouping
- `WeatherSource` — `requestWeather()` returns `Observable<WeatherWrapper>`
- `LocationSearchSource` — location search
- `ReverseGeocodingSource` — lat/lng → address
- `LocationSource` — GPS/network location
- `HttpSource` — marks an HTTP-based source; gets OkHttp client via Hilt
- `ConfigurableSource` — exposes user-configurable preferences (API key, custom URL, etc.)
- `NonFreeNetSource` — marks sources requiring non-free network connections

All Source implementations live under `app/src/main/kotlin/org/breezyweather/sources/<sourceid>/` and are injected into `SourceManager` via Hilt constructor injection.

### MVVM with StateFlow

ViewModels expose `StateFlow<T>` observed by Fragments/Activities via `collectAsState()` or `repeatOnLifecycle`. No LiveData except for legacy `BusLiveData` in `common/activities/livedata/`.

### RxJava 3 → Coroutines Bridge

`WeatherSource.requestWeather()` and location sources return `Observable<T>`. `RefreshHelper` bridges this with `awaitFirstOrElse()` from `kotlinx-coroutines-rx3`.

### Hilt DI Modules

| Module | Scope | Provides |
|--------|-------|---------|
| `HttpModule` | Singleton | `OkHttpClient`, `Retrofit.Builder` variants (`JsonClient`, `XmlClient`) |
| `DbModule` | Singleton | `DatabaseHandler`, `LocationRepository`, `WeatherRepository` |
| `RxModule` | Singleton | `SchedulerTransformer` |

Files: `app/src/main/kotlin/org/breezyweather/common/di/`

---

## Data Flow

### Weather Refresh (User-triggered or Background)

```
User taps refresh / WeatherUpdateJob fires
        │
        ▼
MainActivityViewModel.refreshWeather()
        │  calls
        ▼
RefreshHelper.requestWeather(location)
        │  queries SourceManager for weather/location sources
        │  calls source.requestWeather() → Observable<WeatherWrapper> (RxJava 3)
        │  awaitFirstOrElse bridges to coroutine
        ▼
Source implementation (e.g., OpenMeteoService, NominatimService)
        │  OkHttp + Retrofit HTTP call
        │  JSON/XML → domain WeatherWrapper
        ▼
RefreshHelper merges WeatherWrapper into domain Weather model
        │  persists via WeatherRepository.insertWeather()
        │  which calls DatabaseHandler (SQLDelight)
        ▼
WeatherRepository emits updated Weather
        │
        ▼
MainActivityViewModel._currentLocation StateFlow updated
        │
        ▼
HomeFragment / RecyclerView adapters re-render
```

### Location Search Flow

```
User types in SearchActivity
        │
        ▼
SearchViewModel → SearchActivityRepository
        │  calls SourceManager.getLocationSearchSource()
        │  calls source.requestLocationSearch() → Observable<List<Location>>
        ▼
NominatimService (or GeoNamesService, etc.) HTTP call
        │
        ▼
Results displayed in SearchActivity RecyclerView
        │  user selects → LocationRepository.insertLocation()
        ▼
MainActivity refreshes location list
```

### Reverse Geocoding (Current Location)

```
Android GPS provider → RefreshHelper.requestLocation()
        │
        ▼
AndroidLocationService (GPS coordinates)
        │
        ▼
ReverseGeocodingSource (NominatimService / AndroidGeocoderService)
        │  lat/lng → LocationAddressInfo
        ▼
Location.copy(locationAddressInfo = ...)
        │  persisted via LocationRepository
        ▼
UI displays location name
```

---

## Navigation

Fragment-based navigation hosted in `MainActivity`:

- `HomeFragment` — main weather display for selected location
- `ManagementFragment` — location list management
- `MainModuleFragment` — wraps above fragments

Separate Activities for:

- `SearchActivity` — location search (`ui/search/`)
- `AlertActivity` — weather alerts list (`ui/alert/`)
- `DetailsActivity` — day-level detail (`ui/details/`)
- Settings activities in `ui/settings/activities/`
- `LiveWallpaperConfigActivity` — wallpaper config (`wallpaper/`)

Settings screens use Compose Navigation (`SettingsScreenRouter.kt`).

---

## Module Dependency Graph

```
:weather-unit
      ▲
      │  (implementation)
:domain
      ▲
      │  (implementation)
:data
      ▲
      │  (implementation)
:app ←── :maps-utils
     ←── :ui-weather-view
     ←── :weather-unit  (direct, for unit formatting in UI)
```

Concrete direction: `app` depends on `data`, `domain`, `maps-utils`, `ui-weather-view`, `weather-unit`. `data` depends on `domain` and `weather-unit`. `domain` depends on `weather-unit`. `maps-utils` and `ui-weather-view` are standalone utility modules with no cross-module dependencies.

---

## Architectural Constraints

- **Threading:** Main thread for UI; Coroutines `Dispatchers.IO` for DB and network via `launchIO()`; RxJava 3 with `SchedulerTransformer` at source boundary.
- **Global state:** `BreezyWeather.instance` singleton holds activity stack reference. `SettingsManager` is a Hilt singleton.
- **RxJava boundary:** Only Sources use RxJava 3 Observables. All other async code is Kotlin Coroutines.
- **No Use Case layer:** Domain logic is distributed between ViewModels and `RefreshHelper` rather than dedicated UseCase classes. The `:domain` module contains only models and enums (not interactors).

---

## Anti-Patterns

### Domain-in-app duplication

**What happens:** `app/src/main/kotlin/org/breezyweather/domain/` mirrors `domain/src/main/kotlin/breezyweather/domain/` — domain-adjacent code (e.g., `SettingsManager`, `CurrentLocationStore`, source extensions) lives in the app module's `domain/` package instead of the `:domain` library module.
**Why it's wrong:** Blurs the Clean Architecture boundary; settings singletons couple domain logic to Android.
**Do this instead:** Keep only pure Kotlin domain models in `:domain`; move Android-coupled settings to `:app`'s `common/` or `data/` package explicitly.

### RxJava + Coroutines mixing

**What happens:** `WeatherSource` returns `Observable<WeatherWrapper>` (RxJava 3) while the rest of the app is Coroutines; bridged via `awaitFirstOrElse`.
**Why it's wrong:** Two async paradigms add cognitive overhead and make error propagation inconsistent.
**Do this instead:** New sources can use `suspend fun` returning `WeatherWrapper` directly; the bridge should be considered transitional.

---

## Error Handling

**Strategy:** `RefreshError` sealed class returned alongside results (not thrown). Sources report errors via `WeatherResult` / `LocationResult` wrappers. The ViewModel collects errors and shows Snackbars via `SnackbarHelper`.

**Exception types:** `ApiKeyMissingException`, `NoNetworkException`, `LocationException`, `ReverseGeocodingException`, `WeatherException`, `InvalidLocationException` — all in `app/src/main/kotlin/org/breezyweather/common/exceptions/`.

---

## Cross-Cutting Concerns

**Logging:** `LogHelper` wrapper around `android.util.Log` — `app/src/main/kotlin/org/breezyweather/common/utils/helpers/LogHelper.kt`
**Settings:** `SettingsManager` singleton (Hilt) reads/writes `SharedPreferences` — `app/src/main/kotlin/org/breezyweather/domain/settings/SettingsManager.kt`
**Theme:** Material You dynamic color + day/night theme switching in `BreezyActivity`; custom weather background animation in `:ui-weather-view`

---

*Architecture analysis: 2026-05-13*
