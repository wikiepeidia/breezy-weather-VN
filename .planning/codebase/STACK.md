# Tech Stack

**Analysis Date:** 2026-05-13

## Language & Runtime

- **Language:** Kotlin 2.3.21
- **JVM Target:** 1.8 (Java 8 compatibility)
- **compileSdk / targetSdk:** 36 (Android 16)
- **minSdk:** 23 (Android 6.0 Marshmallow)
- **Build Tools:** 36.0.0
- **App ID:** `io.github.wikiepeidia.vnweather`
- **Version:** 6.2.1 (versionCode 60201)
- **Build system:** Gradle with Kotlin DSL (`build.gradle.kts`), AGP 9.2.0

Defined in `buildSrc/src/main/kotlin/breezy/buildlogic/AndroidConfig.kt`.

## Project Modules

Defined in `settings.gradle.kts`:

| Module | Purpose |
|--------|---------|
| `:app` | Main application module |
| `:data` | SQLDelight database layer, repositories |
| `:domain` | Domain models, source interfaces |
| `:maps-utils` | Fork of Google Maps Android Utils (GeoJSON, polygon math) |
| `:ui-weather-view` | Custom animated weather views |
| `:weather-unit` | Unit conversion library (temperature, speed, pressure, etc.) |

## Frameworks & Core Libraries

| Library | Version | Purpose |
|---------|---------|---------|
| Jetpack Compose BOM | 2026.04.01 | UI toolkit (version management) |
| Material3 | 1.5.0-alpha18 | Material You design components |
| Navigation Compose | 2.9.8 | In-app navigation |
| Activity | 1.13.0 | Activity / Compose entry point |
| Accompanist Permissions | 0.37.3 | Compose runtime permissions |
| AppCompat | 1.7.1 | Legacy View compatibility |
| Core KTX | 1.18.0 | Android KTX extensions |
| Core SplashScreen | 1.2.0 | Splash screen API |
| RecyclerView | 1.4.0 | Legacy list views |
| CardView | 1.0.0 | Legacy card UI |
| SwipeRefreshLayout | 1.2.0 | Pull-to-refresh |
| Vico | 2.4.4 | Compose + View chart library (`compose-m3`, `views`) |
| AdaptiveIconView | v0.0.3 | Adaptive icon rendering |
| AboutLibraries | 14.1.0 | Open-source attribution screen |
| Preference KTX | 1.2.1 | Settings preferences |

## Dependency Injection

**Framework:** Dagger Hilt 2.59.2 + AndroidX Hilt 1.3.0

- `@HiltAndroidApp` on `BreezyWeather` application class (`app/src/main/kotlin/org/breezyweather/BreezyWeather.kt`)
- `@InstallIn(SingletonComponent::class)` modules:
  - `HttpModule` — OkHttp, Retrofit builders, JSON/XML serializers (`app/src/main/kotlin/org/breezyweather/common/di/HttpModule.kt`)
  - `DbModule` — SQLDelight driver + repositories (`app/src/main/kotlin/org/breezyweather/common/di/DbModule.kt`)
  - `RxModule` — RxJava schedulers (`app/src/main/kotlin/org/breezyweather/common/di/RxModule.kt`)
- KSP 2.3.7 used for annotation processing (replaces kapt)
- `@Inject constructor` on all Service classes (sources, repositories, use cases)
- `hilt-work` for WorkManager integration

## Networking

| Library | Version | Notes |
|---------|---------|-------|
| OkHttp | 5.3.2 | HTTP client, 30s connect/read, 45s write |
| OkHttp Logging Interceptor | 5.3.2 | Debug BODY logging |
| OkHttp TLS | 5.3.2 | Let's Encrypt root CA injection for API < 24 |
| Retrofit | 3.0.0 | REST client |
| kotlinx.serialization JSON | 1.11.0 | JSON serialization/deserialization |
| kotlinx.serialization XML | 0.91.3 | XML serialization (pdvrieze/xmlutil) |
| JJWT | 0.13.0 | JWT signing (Météo-France only, `basicImplementation`) |
| RxJava 3 call adapter | (bundled with retrofit) | Retrofit → RxJava3 `Observable` |

**Named Retrofit builders** (from `HttpModule.kt`):

- `@Named("JsonClient")` — JSON-based APIs (most sources)
- `@Named("XmlClient")` — XML-based APIs (some national services)

**OkHttp settings:** 50 MB disk cache at `cacheDir/http_cache`, logging level BODY in debug / NONE in release.

## Storage / Database

| Library | Version | Purpose |
|---------|---------|---------|
| SQLDelight | 2.3.2 | Type-safe SQL schema + queries |
| sqlite-android (requery) | 3.49.0 | Bundled newer SQLite for Android < API 29 |
| SQLite framework (AndroidX) | 2.6.2 | SupportSQLiteOpenHelper abstraction |
| SQLite KTX | 2.6.2 | SQLite Kotlin extensions |

SQLDelight dialect: `sqlite-3-38-dialect`. Database schema defined in `data/src/main/sqldelight/`. Paging via `sqldelight-android-paging` (Paging 3).

Entities: `Locations`, `Weathers`, `Dailys`, `Hourlys`, `Minutelys`, `Alerts`, `Normals`, `location_parameters`.

Repositories: `LocationRepository`, `WeatherRepository` (`data/src/main/kotlin/breezyweather/data/`).

## UI Framework

- **Primary:** Jetpack Compose (Material3, BOM 2026.04.01)
  - Compose screens under `app/src/main/kotlin/org/breezyweather/ui/`
  - ViewBinding enabled for legacy View-based fragments
- **Charts:** Vico 2.4.4 (Compose M3 + Views variants)
- **Animations:** Custom `ui-weather-view` module (canvas-based weather animations)
- **Icons:** `compose.material.icons` (Material icons extended)
- **Theme:** `BreezyWeatherTheme` (defined in `app/src/main/res/`)

## Async / Concurrency

| Library | Version | Usage |
|---------|---------|-------|
| RxJava 3 | 3.1.12 | All network/data streams (sources return `Observable<T>`) |
| RxAndroid | 3.0.2 | `AndroidSchedulers.mainThread()` |
| kotlinx.coroutines | 1.10.2 | Repositories, WorkManager jobs, suspend functions |
| kotlinx-coroutines-rx3 | 1.10.2 | Bridge: `rxObservable {}`, `Observable.awaitFirst()` |

Network sources use RxJava3 `Observable`. Database and background jobs use coroutines/suspend. Migration to pure coroutines is noted as a TODO in `HttpModule.kt`.

## Background Jobs

- **WorkManager** 2.11.2 — weather refresh scheduling (`app/src/main/kotlin/org/breezyweather/background/weather/`)
- **Hilt WorkManager integration** — `@HiltWorker` on worker classes
- **AlarmManager watchdog** — fallback wakeup (`background/watchdog/`)
- **Foreground services** — declared in `AndroidManifest.xml` for data sync and special use

## Build Variants / Flavors

**Dimension:** `default`

| Flavor | Source Set | Description |
|--------|------------|-------------|
| `basic` | `src/src_nonfreenet` + `src/res_nonfreenet` | Includes proprietary/API-key-gated weather sources (AccuWeather, OpenWeather, etc.) |
| `freenet` | `src/src_freenet` + `src/res_freenet` | Only free/open network sources (Open-Meteo, MET.no, etc.) |

Both flavors include either `src/res_breezy` or `src/res_fork` depending on the `Config.isBreezy` flag.

**Build types:** `debug` (`.debug` suffix, commit count in version name), `release` (minify + shrink resources, ProGuard).

**ABI splits:** `armeabi-v7a`, `arm64-v8a`, `x86`, `x86_64` + universal APK.

## Build Tooling

| Tool | Version | Purpose |
|------|---------|---------|
| AGP | 9.2.0 | Android Gradle Plugin |
| KSP | 2.3.7 | Kotlin Symbol Processing (replaces KAPT) |
| Spotless | 8.4.0 | Code formatting enforcement |
| KTLint | 1.8.0 | Kotlin linting |
| AboutLibraries Gradle plugin | 14.1.0 | License metadata collection |
| SQLDelight Gradle plugin | 2.3.2 | SQL schema → Kotlin code generation |

Custom build logic plugins in `buildSrc/src/main/kotlin/`:

- `breezy.android.application` — base app configuration
- `breezy.android.application.compose` — Compose setup
- `breezy.library` — library module conventions

## Configuration

**Key config files:**

- `gradle/libs.versions.toml` — version catalog (all versions centralised)
- `local.properties` — API keys per source (e.g. `breezy.accu.key`, `breezy.openweather.key`) and default source overrides
- `gradle.properties` — app metadata (report issue link, source code link, GitHub org/repo, privacy policy)
- `buildSrc/src/main/kotlin/breezy/buildlogic/AndroidConfig.kt` — SDK version constants
- `buildSrc/src/main/kotlin/breezy/buildlogic/BuildConfig.kt` — `Config.isBreezy` flag

---

*Stack analysis: 2026-05-13*
