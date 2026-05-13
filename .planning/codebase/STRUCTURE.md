# Project Structure

**Analysis Date:** 2026-05-13

---

## Modules

| Module | Purpose | Root Package |
|--------|---------|-------------|
| `:app` | Main application — UI, sources, background jobs, DI wiring | `org.breezyweather` |
| `:data` | SQLDelight database, repositories for Location and Weather | `breezyweather.data` |
| `:domain` | Pure Kotlin domain models (Location, Weather, SourceFeature) | `breezyweather.domain` |
| `:weather-unit` | Weather unit types (temperature, pressure, speed, etc.) and formatting | `org.breezyweather.unit` |
| `:maps-utils` | Vendored Google Maps Android Utils (geometry, GeoJSON) | `com.google.maps.android` |
| `:ui-weather-view` | Material animated weather background view (canvas-based) | `org.breezyweather.ui.theme.weatherView` |

---

## Module Dependency Graph

```
:weather-unit  (no deps)
     ▲
     │
:domain  (depends on :weather-unit)
     ▲
     │
:data   (depends on :domain, :weather-unit)
     ▲
     │
:app  ←── :data
     ←── :domain
     ←── :weather-unit
     ←── :maps-utils
     ←── :ui-weather-view
```

---

## :app module

```
app/src/main/kotlin/org/breezyweather/
├── BreezyWeather.kt                  # Application class, activity lifecycle tracking
├── Migrations.kt                     # DB/preference migration logic
├── background/
│   ├── forecast/                     # Forecast notification background work
│   ├── interfaces/                   # Background service interfaces
│   ├── provider/                     # ContentProvider for widget data
│   ├── receiver/
│   │   └── widget/                   # Broadcast receivers for widget updates
│   ├── updater/
│   │   ├── data/                     # App update check data models
│   │   ├── interactor/               # App update check use case
│   │   └── model/                    # App update check models
│   ├── watchdog/                     # Watchdog for background refresh
│   └── weather/
│       ├── WeatherUpdateJob.kt       # Scheduled weather refresh (WorkManager)
│       └── WeatherUpdateNotifier.kt  # Progress/result notifications
├── common/
│   ├── actionmodecallback/           # Action mode support
│   ├── activities/
│   │   ├── BreezyActivity.kt         # Base Activity (edge-to-edge, theme)
│   │   ├── BreezyFragment.kt         # Base Fragment
│   │   ├── BreezyViewModel.kt        # Base ViewModel
│   │   └── livedata/
│   │       └── BusLiveData.kt        # Event bus LiveData bridge
│   ├── bus/
│   │   └── EventBus.kt               # App-wide event bus
│   ├── di/
│   │   ├── DbModule.kt               # Hilt: DB, repositories
│   │   ├── HttpModule.kt             # Hilt: OkHttp, Retrofit builders
│   │   └── RxModule.kt               # Hilt: RxJava scheduler transformer
│   ├── exceptions/                   # Typed exception classes
│   ├── extensions/                   # Kotlin extension functions (Context, View, etc.)
│   ├── options/
│   │   └── appearance/               # Appearance preference enums
│   ├── preference/                   # Preference UI helpers
│   ├── rxjava/
│   │   ├── ObserverContainer.kt
│   │   └── SchedulerTransformer.kt   # IO → Main thread transformer
│   ├── serializer/                   # Custom Kotlinx Serialization adapters
│   ├── snackbar/                     # Custom Snackbar infrastructure
│   ├── source/                       # Source interfaces (plugin contracts)
│   │   ├── Source.kt                 # Base source interface
│   │   ├── WeatherSource.kt          # Weather data source
│   │   ├── LocationSearchSource.kt   # Location search source
│   │   ├── ReverseGeocodingSource.kt # Reverse geocoding source
│   │   ├── HttpSource.kt             # HTTP source base
│   │   ├── ConfigurableSource.kt     # User-configurable source (API key, URL)
│   │   ├── LocationSource.kt         # GPS/network location source
│   │   ├── BroadcastSource.kt        # GadgetBridge broadcast source
│   │   ├── NonFreeNetSource.kt       # Marks non-free network sources
│   │   ├── FeatureSource.kt          # Per-feature priority support
│   │   ├── RefreshError.kt           # Error sealed class
│   │   ├── WeatherResult.kt          # Weather fetch result wrapper
│   │   └── LocationResult.kt         # Location fetch result wrapper
│   └── utils/
│       └── helpers/                  # AsyncHelper, IntentHelper, LogHelper, etc.
├── data/                             # App-module data utilities (not :data module)
│   └── Contributors.kt
├── domain/                           # App-module domain utilities (not :domain module)
│   ├── location/
│   │   └── model/                    # Location extension functions
│   ├── settings/
│   │   ├── SettingsManager.kt        # SharedPreferences wrapper (Hilt singleton)
│   │   ├── SourceConfigStore.kt      # Per-source config persistence
│   │   ├── CurrentLocationStore.kt   # Current location caching
│   │   └── ConfigStore.kt            # Generic config store
│   ├── source/
│   │   └── resourceName              # SourceContinent → string resource
│   └── weather/
│       └── index/                    # Weather index calculations (UV, AQI, etc.)
├── remoteviews/
│   ├── common/                       # Shared widget utilities
│   ├── config/                       # Widget configuration activities
│   ├── presenters/
│   │   ├── AbstractRemoteViewsPresenter.kt
│   │   ├── DayWidgetIMP.kt           # Today widget
│   │   ├── DailyTrendWidgetIMP.kt    # 7-day trend widget
│   │   ├── HourlyTrendWidgetIMP.kt   # Hourly trend widget
│   │   ├── ClockDay*.kt              # Clock+weather widgets (multiple layouts)
│   │   ├── MaterialYou*WidgetIMP.kt  # Material You adaptive widgets
│   │   ├── MultiCityWidgetIMP.kt     # Multi-city widget
│   │   └── notification/             # Persistent weather notification
│   └── trend/                        # Trend chart rendering for widgets
├── sources/
│   ├── RefreshHelper.kt              # Weather + location refresh orchestration
│   ├── SourceManager.kt              # Registry of all injected Source instances
│   ├── accu/                         # AccuWeather
│   ├── aemet/                        # AEMET (Spain)
│   ├── android/                      # Android Geocoder + Location
│   ├── atmo/                         # Atmo (France regional air quality)
│   ├── baiduip/                      # Baidu IP location
│   ├── brightsky/                    # BrightSky (Germany DWD)
│   ├── china/                        # China national service
│   ├── climweb/                      # ClimWeb (African NMS network, 15+ services)
│   ├── common/xml/                   # Shared XML parsing utilities
│   ├── debug/                        # Debug/test source
│   ├── geonames/                     # GeoNames geocoding
│   ├── metno/                        # Met.no (Norway, global)
│   ├── nominatim/
│   │   ├── NominatimService.kt       # Nominatim + LocationIQ unified source
│   │   ├── NominatimApi.kt           # Retrofit API interface
│   │   └── json/                     # Response DTOs
│   ├── openmeteo/                    # Open-Meteo (global, free)
│   ├── openweather/                  # OpenWeatherMap
│   └── [40+ more sources]            # National meteorological services worldwide
├── ui/
│   ├── about/                        # About / licenses screen
│   ├── alert/
│   │   ├── AlertActivity.kt          # Hilt entry point
│   │   ├── AlertViewModel.kt         # Alert list state
│   │   ├── AlertScreen.kt            # Compose UI
│   │   └── AlertUiState.kt
│   ├── common/
│   │   ├── adapters/                 # Shared RecyclerView adapters
│   │   ├── behaviors/                # Scroll behaviors
│   │   ├── charts/                   # Chart view components
│   │   ├── composables/              # Shared Compose composables
│   │   ├── decorations/              # RecyclerView item decorations
│   │   ├── images/                   # Coil image loaders / transformations
│   │   └── widgets/
│   │       ├── astro/                # Sun/moon arc widget
│   │       ├── insets/               # Window inset utilities
│   │       ├── slidingItem/          # Swipe-to-delete list item
│   │       └── trend/
│   │           ├── chart/            # Trend chart view
│   │           └── item/             # Trend RecyclerView items
│   ├── details/
│   │   ├── DetailsActivity.kt
│   │   ├── DetailsViewModel.kt
│   │   ├── DetailsScreen.kt          # Compose UI
│   │   ├── DetailsUiState.kt
│   │   └── components/               # Detail card composables
│   ├── main/
│   │   ├── MainActivity.kt           # Host activity
│   │   ├── MainActivityViewModel.kt  # Primary ViewModel (StateFlow-based)
│   │   ├── MainActivityModels.kt     # UI state data classes
│   │   ├── adapters/
│   │   │   ├── location/             # Location list RecyclerView adapter
│   │   │   ├── main/
│   │   │   │   └── holder/           # Weather card ViewHolders (one per data type)
│   │   │   └── trend/
│   │   │       ├── daily/            # Daily trend adapter
│   │   │       └── hourly/           # Hourly trend adapter
│   │   ├── dialogs/                  # In-screen dialogs
│   │   ├── fragments/
│   │   │   ├── HomeFragment.kt       # Primary weather display
│   │   │   ├── ManagementFragment.kt # Location list management
│   │   │   └── MainModuleFragment.kt # Navigation host fragment
│   │   ├── layouts/                  # Custom layout managers
│   │   ├── utils/
│   │   │   ├── RefreshErrorType.kt   # Error type enum
│   │   │   └── StatementManager.kt   # Permission / onboarding statement state
│   │   └── widgets/                  # Main screen custom views
│   ├── search/
│   │   ├── SearchActivity.kt         # Location search
│   │   ├── SearchViewModel.kt
│   │   ├── SearchActivityRepository.kt
│   │   └── LoadableLocationStatus.kt
│   ├── settings/
│   │   ├── activities/               # Settings Activity hosts
│   │   ├── adapters/                 # Settings list adapters
│   │   ├── compose/                  # Compose settings screens
│   │   │   ├── SettingsScreenRouter.kt
│   │   │   ├── AppearanceSettingsScreen.kt
│   │   │   ├── WeatherSourcesSettingsScreen.kt
│   │   │   ├── UnitSettingsScreen.kt
│   │   │   └── [8 more screens]
│   │   ├── dialogs/                  # Settings dialogs
│   │   └── preference/
│   │       └── composables/          # Composable preference row items
│   ├── theme/
│   │   ├── compose/                  # Compose theme (MaterialTheme wrapper)
│   │   ├── resource/
│   │   │   ├── providers/            # Icon pack / resource providers
│   │   │   └── utils/
│   │   └── weatherView/
│   │       └── WeatherViewController.kt  # Bridge to :ui-weather-view
└── wallpaper/
    ├── MaterialLiveWallpaperService.kt   # Live wallpaper service
    └── LiveWallpaperConfigActivity.kt
```

---

## :data module

```
data/src/main/
├── kotlin/breezyweather/data/
│   ├── DatabaseHandler.kt            # Coroutine-aware SQLDelight query executor
│   ├── AndroidDatabaseHandler.kt     # Android implementation
│   ├── DatabaseAdapter.kt            # SQLDelight driver setup
│   ├── TransactionContext.kt         # Coroutine transaction scope
│   ├── location/
│   │   ├── LocationRepository.kt     # CRUD: Location entities
│   │   └── LocationMapper.kt         # SQLDelight → domain model mapping
│   └── weather/
│       ├── WeatherRepository.kt      # CRUD: Weather + sub-entities
│       └── WeatherMapper.kt          # SQLDelight → domain model mapping
└── sqldelight/breezyweather/
    ├── data/                         # .sq files (SQL queries for locations, weather)
    └── migrations/                   # Version migration .sqm files
```

---

## :domain module

```
domain/src/main/kotlin/breezyweather/domain/
├── location/
│   └── model/
│       ├── Location.kt               # Core location entity (lat, lng, timezone, sources)
│       └── LocationAddressInfo.kt    # Reverse geocoding result
├── source/
│   ├── SourceContinent.kt            # Enum: continent grouping for UI
│   └── SourceFeature.kt             # Enum: FORECAST, CURRENT, AIR_QUALITY, POLLEN, etc.
└── weather/
    ├── model/
    │   ├── Weather.kt                # Aggregate root: current + daily + hourly + alerts
    │   ├── Current.kt
    │   ├── Daily.kt
    │   ├── Hourly.kt
    │   ├── Minutely.kt
    │   ├── Alert.kt
    │   ├── AirQuality.kt
    │   ├── Pollen.kt
    │   ├── Temperature.kt
    │   ├── Wind.kt
    │   ├── Precipitation.kt
    │   ├── UV.kt
    │   ├── Astro.kt
    │   ├── Normals.kt
    │   └── [12 more model files]
    ├── reference/
    │   ├── WeatherCode.kt            # Enum: weather condition codes
    │   ├── AlertSeverity.kt          # Enum: alert severity levels
    │   └── Month.kt                  # Enum: month reference
    └── wrappers/
        ├── WeatherWrapper.kt         # Source output: wraps Weather for merging
        ├── CurrentWrapper.kt
        ├── DailyWrapper.kt
        ├── HourlyWrapper.kt
        ├── AirQualityWrapper.kt
        ├── PollenWrapper.kt
        ├── HalfDayWrapper.kt
        └── TemperatureWrapper.kt
```

---

## :weather-unit module

```
weather-unit/src/main/kotlin/org/breezyweather/unit/
├── WeatherUnit.kt                    # Base interface for all unit enums
├── WeatherValue.kt                   # Value + unit pair
├── SdkCheck.kt
├── computing/                        # Unit conversion math
├── formatting/                       # Locale-aware value formatting
├── temperature/                      # TemperatureUnit enum + conversions
├── speed/                            # SpeedUnit (m/s, km/h, mph, etc.)
├── pressure/                         # PressureUnit
├── precipitation/                    # PrecipitationUnit
├── distance/                         # DistanceUnit
├── duration/                         # DurationUnit
├── pollen/                           # PollenUnit
├── pollutant/                        # PollutantUnit (µg/m³, etc.)
└── ratio/                            # RatioUnit (%, etc.)
```

---

## :ui-weather-view module

```
ui-weather-view/src/main/kotlin/org/breezyweather/ui/theme/weatherView/
├── materialWeatherView/
│   ├── MaterialWeatherView.kt        # SurfaceView with canvas animation
│   ├── MaterialPainterView.kt        # Painter abstraction
│   ├── MaterialWeatherThemeDelegate.kt
│   ├── WeatherImplementorFactory.kt  # Maps WeatherCode → Implementor
│   ├── IntervalComputer.kt           # Frame timing
│   ├── DelayRotateController.kt      # Sensor-based rotation smoothing
│   └── implementor/                  # One class per weather condition animation
```

---

## :maps-utils module

```
maps-utils/src/main/kotlin/com/google/maps/android/
├── SphericalUtil.kt                  # Haversine distance, bearing calculations
├── model/
│   └── LatLng.kt
└── data/geojson/                     # GeoJSON parsing (used for country data)
```

Used by `RefreshHelper` and `MainActivityViewModel` for distance-based location checks.

---

## Key Files

| File | Purpose |
|------|---------|
| `app/src/main/kotlin/org/breezyweather/BreezyWeather.kt` | Application class |
| `app/src/main/kotlin/org/breezyweather/ui/main/MainActivity.kt` | Main entry Activity |
| `app/src/main/kotlin/org/breezyweather/ui/main/MainActivityViewModel.kt` | Primary ViewModel |
| `app/src/main/kotlin/org/breezyweather/sources/SourceManager.kt` | Source plugin registry |
| `app/src/main/kotlin/org/breezyweather/sources/RefreshHelper.kt` | Weather refresh orchestration |
| `app/src/main/kotlin/org/breezyweather/sources/nominatim/NominatimService.kt` | Nominatim/LocationIQ source |
| `app/src/main/kotlin/org/breezyweather/common/di/HttpModule.kt` | Hilt HTTP setup |
| `app/src/main/kotlin/org/breezyweather/common/di/DbModule.kt` | Hilt DB setup |
| `data/src/main/kotlin/breezyweather/data/location/LocationRepository.kt` | Location persistence |
| `data/src/main/kotlin/breezyweather/data/weather/WeatherRepository.kt` | Weather persistence |
| `domain/src/main/kotlin/breezyweather/domain/weather/model/Weather.kt` | Aggregate root model |
| `domain/src/main/kotlin/breezyweather/domain/location/model/Location.kt` | Location model |
| `settings.gradle.kts` | Module declarations |
| `app/build.gradle.kts` | App build config, flavors, signing |
| `gradle/libs.versions.toml` | Dependency version catalog |

---

## Naming Conventions

**Files:**

- Activities: `<Feature>Activity.kt` (e.g., `SearchActivity.kt`)
- ViewModels: `<Feature>ViewModel.kt`
- Compose screens: `<Feature>Screen.kt`
- UI state: `<Feature>UiState.kt`
- Widget presenters: `<Name>WidgetIMP.kt`
- Source services: `<SourceId>Service.kt`
- Retrofit API interfaces: `<SourceId>Api.kt`

**Packages:**

- Source implementations: `sources/<sourceid>/`
- Domain models: `domain/weather/model/`, `domain/location/model/`
- Hilt modules: `common/di/`

---

## Where to Add New Code

**New weather/geocoding source:**

1. Create `app/src/main/kotlin/org/breezyweather/sources/<sourceid>/` directory
2. Add `<SourceId>Service.kt` implementing relevant Source interfaces
3. Add `<SourceId>Api.kt` Retrofit interface (if HTTP)
4. Add JSON/XML response DTOs in `<sourceid>/json/` or `<sourceid>/xml/`
5. Register in `SourceManager.kt` constructor parameter list

**New UI screen (Compose):**

- Add `<Feature>Activity.kt`, `<Feature>ViewModel.kt`, `<Feature>Screen.kt`, `<Feature>UiState.kt` in `app/src/main/kotlin/org/breezyweather/ui/<feature>/`

**New weather card on main screen:**

- Add a new `<Name>ViewHolder.kt` in `app/src/main/kotlin/org/breezyweather/ui/main/adapters/main/holder/`
- Register in the main adapter

**New domain model field:**

- Add to relevant data class in `domain/src/main/kotlin/breezyweather/domain/weather/model/`
- Add wrapper field in `domain/src/main/kotlin/breezyweather/domain/weather/wrappers/`
- Update SQLDelight schema in `data/src/main/sqldelight/breezyweather/data/`
- Add migration in `data/src/main/sqldelight/breezyweather/migrations/`
- Update `WeatherMapper.kt` in `data/src/main/kotlin/breezyweather/data/weather/`

**New unit type:**

- Add enum in `weather-unit/src/main/kotlin/org/breezyweather/unit/<type>/`
- Add conversion logic in `computing/`
- Add formatting in `formatting/`

---

## Special Directories

**`app/src/res_breezy/`, `res_fork/`, `res_freenet/`, `res_nonfreenet/`:**

- Purpose: Flavor-specific resources (branding assets, API keys config)
- Generated: No
- Committed: Yes

**`app/src/src_freenet/`, `src_nonfreenet/`:**

- Purpose: Flavor-specific source sets for free-net vs non-free-net build variants
- Generated: No

**`app/work/`:**

- Purpose: Country boundary GeoJSON data (`ne_50m_admin_0_countries.json`) for NaturalEarth source
- Generated: No
- Committed: Yes

**`buildSrc/`:**

- Purpose: Convention plugins (`breezy.android.application`, `breezy.android.application.compose`, etc.)
- Generated: No

**`data/src/main/sqldelight/`:**

- Purpose: SQLDelight `.sq` query files and migration `.sqm` files; Kotlin interfaces generated at build time
- Generated: Kotlin output in `data/build/generated/`

---

*Structure analysis: 2026-05-13*
