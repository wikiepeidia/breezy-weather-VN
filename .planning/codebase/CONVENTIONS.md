# Code Conventions

**Analysis Date:** 2026-05-13

## Language Style

**Primary language:** Kotlin (all production code under `app/src/main/kotlin/`, `domain/src/main/kotlin/`, `data/src/main/kotlin/`)

**Secondary language:** Tests are placed under both `app/src/test/kotlin/` (Kotlin package) and `app/src/test/java/` (Java package layout — still Kotlin files). Prefer the `kotlin/` tree for new tests.

**Kotlin version:** 2.3.21 (see `gradle/libs.versions.toml`)

**JVM target:** JVM 1.8 (defined in `buildSrc/src/main/kotlin/breezy/buildlogic/AndroidConfig.kt`)

**SDK:** compileSdk 36, minSdk 23, targetSdk 36

## Code Formatting

**Tool:** [Spotless](https://github.com/diffplug/spotless) + [ktlint](https://pinterest.github.io/ktlint/)

- Code style: `intellij_idea` (set in `.editorconfig` via `ktlint_code_style`)
- Formatter applied via `breezy.code.lint` convention plugin: `buildSrc/src/main/kotlin/breezy.code.lint.gradle.kts`
- Run check: `./gradlew spotlessCheck`
- Run fix: `./gradlew spotlessApply`

**Key formatting rules (from `.editorconfig`):**

| Setting | Value |
|---------|-------|
| Indent style | space |
| Indent size (default) | 2 spaces |
| Indent size (`.kt`, `.kts`, `.xml`, `.sq`) | 4 spaces |
| Max line length | 120 characters |
| Trailing comma (declarations) | allowed (`ij_kotlin_allow_trailing_comma = true`) |
| Trailing comma (call sites) | disallowed |
| Star imports | disabled (threshold set to `2147483647`) |
| Final newline | required |
| Trailing whitespace | trimmed |

**Disabled ktlint rules:**

- `ktlint_standard_class-signature`
- `ktlint_standard_comment-wrapping`
- `ktlint_standard_discouraged-comment-location`
- `ktlint_standard_function-expression-body`
- `ktlint_standard_function-signature`
- `ktlint_standard_type-argument-comment`
- `ktlint_standard_type-parameter-comment`
- `ktlint_standard_blank-line-between-when-conditions`

**Composable function naming exception:** `ktlint_function_naming_ignore_when_annotated_with = Composable` — `@Composable` functions may use PascalCase.

## Naming Conventions

**Files:**

- Kotlin source files use PascalCase matching the primary class/object/interface name
- One top-level declaration per file (standard Kotlin convention)
- Examples: `NominatimService.kt`, `WeatherSource.kt`, `BreezyActivity.kt`, `ContextExtensions.kt`

**Classes / Interfaces / Objects:**

- PascalCase: `AirQuality`, `WeatherSource`, `BreezyWeather`, `NominatimService`
- Service/source implementations are suffixed with `Service`: `NominatimService`, `AccuService`
- Android components follow Android conventions: `BootReceiver`, `TileService`, `WeatherUpdateJob`
- Widget providers follow `Widget<Type>Provider` pattern: `WidgetDayProvider`, `WidgetTrendDailyProvider`
- Extension file names use `<Subject>Extensions.kt`: `ContextExtensions.kt`

**Functions / Methods:**

- camelCase: `getWindDegree()`, `resolveLocationSearchSource()`, `requestWeather()`
- Boolean properties prefixed with `is`/`has`: `isValid`, `isIndexValid`, `hasPermission()`
- Test functions: either plain camelCase (`getWindDegreeTest`) or backtick-quoted sentence style for clarity

**Variables / Properties:**

- camelCase: `dailyForecast`, `nextHourlyForecast`, `todayIndex`

**Constants:**

- SCREAMING_SNAKE_CASE in companion objects: `PRIORITY_HIGHEST`, `PRIORITY_NONE`, `COMPILE_SDK`

**Packages:**

- All lowercase, dot-separated: `org.breezyweather.common.extensions`, `breezyweather.domain.weather.model`

**Resources:**

- Layouts: `activity_<name>.xml` for activities, `container_main_<section>.xml` for sections
- Values files: `strings.xml`, `colors.xml`, `themes.xml`, `dimens.xml`, `arrays.xml`, `attrs.xml`
- Drawables, fonts, animators follow standard Android snake_case

## Package Organization

**App module** (`app/src/main/kotlin/org/breezyweather/`):

| Package | Contents |
|---------|----------|
| `background/` | Background services, jobs, receivers, widget providers, watchdog |
| `background/forecast/` | Forecast notification jobs |
| `background/receiver/widget/` | All widget `AppWidgetProvider` subclasses |
| `background/updater/` | In-app update checker logic |
| `common/` | Shared utilities, base classes, extensions, DI, options |
| `common/activities/` | `BreezyActivity`, `BreezyFragment` base classes |
| `common/extensions/` | Kotlin extension functions for Android classes |
| `common/source/` | Source interfaces (`WeatherSource`, `LocationSource`, etc.) |
| `common/options/` | Enum/sealed classes for app options (CardDisplay, etc.) |
| `data/` | App-layer repository wrappers |
| `domain/` | App-layer domain services |
| `remoteviews/` | Widget remote view helpers |
| `sources/` | All weather/location source implementations |
| `ui/` | Compose UI screens and ViewModels |
| `wallpaper/` | Live wallpaper support |

**Domain module** (`domain/src/main/kotlin/breezyweather/domain/`):

- `weather/model/` — Pure Kotlin `data class` / `class` weather models
- `location/model/` — Location domain models
- `source/` — Source feature/continent enums

**Data module** (`data/src/main/kotlin/breezyweather/data/`):

- Database handler, adapters, and repository implementations

## Kotlin Patterns Used

**Data classes:**

- Used for pure value models with all-nullable or default-value fields
- Example: `data class Weather(val base: Base = Base(), val current: Current? = null, ...)` in `domain/src/main/kotlin/breezyweather/domain/weather/model/Weather.kt`
- Domain models implement `Serializable` for IPC compatibility

**Regular classes (not data):**

- Used when computed properties and validation are needed
- Example: `class AirQuality(val pM25: ..., ...) : Serializable` with `val isValid: Boolean get() = ...` in `domain/src/main/kotlin/breezyweather/domain/weather/model/AirQuality.kt`

**Sealed classes / interfaces:**

- Used for navigation routing and discriminated unions
- Example: `sealed class SettingsScreenRouter(val route: String)` in `app/src/main/kotlin/org/breezyweather/ui/settings/compose/SettingsScreenRouter.kt`
- Example: `sealed interface Result` in `app/src/main/kotlin/org/breezyweather/background/updater/interactor/GetApplicationRelease.kt`

**Extension functions:**

- Heavily used in `common/extensions/` to augment Android framework classes
- Example: `fun Context.hasPermission(permission: String): Boolean` in `app/src/main/kotlin/org/breezyweather/common/extensions/ContextExtensions.kt`
- Each extension file groups extensions for one receiver type

**Coroutines:**

- `kotlinx-coroutines` 1.10.2 used throughout
- `runTest` from `kotlinx-coroutines-test` used in unit tests
- Coroutine extensions for SQLDelight via `sqldelight-coroutines`

**RxJava3:**

- `Observable<WeatherWrapper>` return type on `WeatherSource.requestWeather()` — source APIs use RxJava3
- `kotlinx-coroutines-rx3` bridges coroutines ↔ RxJava3

**Dependency Injection:**

- Hilt (`@Inject`, `@HiltViewModel`, `@ApplicationContext`) used in Android components
- `dagger.hilt.android.plugin` applied in `app/build.gradle.kts`

**Immutable collections:**

- `kotlinx-collections-immutable` used: `ImmutableList<T>`, `persistentListOf()`, `toImmutableList()`

**Serialization:**

- `kotlinx.serialization` (JSON) for API response JSON models
- JSON models placed in `json/` subpackage within each source package

**Jetpack Compose:**

- All UI is Compose-based (`breezy.android.application.compose` convention plugin)
- `@Composable` functions use PascalCase

**Kotlin Duration:**

- `kotlin.time.Duration.Companion.hours`, `.days`, `.minutes` used for time math in domain logic

## Resource Conventions

**Layouts:**

- `activity_<name>.xml` — Activity root layouts
- `container_main_<section>.xml` — Main screen section containers
- `layout-v26/`, `layout-w640dp/` — API-level and size-specific overrides

**Values:**

- `strings.xml` — UI strings (localized into 20+ languages via `fastlane/metadata/android/`)
- `arrays.xml` — String arrays (option names and their values kept in parallel arrays)
- `themes.xml`, `styles.xml` — Theme and style definitions
- `keys.xml` — Preference key constants
- `ids.xml` — View IDs

**Drawables:**

- `drawable/` — Vector drawables and other assets
- `drawable-hdpi/`, etc. — Density-specific raster assets

## Code Quality Tools

| Tool | Version | Purpose | Config |
|------|---------|---------|--------|
| Spotless | via Gradle plugin | Formatting enforcement | `buildSrc/src/main/kotlin/breezy.code.lint.gradle.kts` |
| ktlint | see `libs.versions.toml` `ktlint` entry | Kotlin linting | `.editorconfig` |
| KSP | `kotlin-ksp` version | Annotation processing (Hilt, Room/SQLDelight) | `app/build.gradle.kts` |

**ProGuard** (`app/proguard-rules.pro`):

- `-keep class org.breezyweather.common.activities.models.**` — Activity model POJOs
- `-keep interface org.breezyweather.sources.**.*` — Source interfaces
- `-keep class org.breezyweather.sources.**.json.**` — JSON model classes (serialization)
- Standard Android component keeps (Services, BroadcastReceivers)
- Release builds: minify + shrink enabled; debug builds: no minification

## License Header

All source files carry an LGPL v3 license header comment block at the top (enforced by convention). New files must include this header. See any existing `.kt` file for the template.

## Contribution Guidelines (from `CONTRIBUTE.md`)

1. **Discuss first** — open an issue or discussion before implementing new features
2. **Linting** — run `./gradlew spotlessCheck` before submitting; fix with `./gradlew spotlessApply`
3. **Single commit** — PRs must contain exactly one commit (squash before submitting)
4. **Rebase on main** — `git fetch upstream && git rebase upstream main`
5. **AI usage** — if AI tools are used, disclose it; contributor is responsible for correctness and licensing compliance
6. **New weather sources** — must use a short lowercase identifier (e.g., `accu`, `openmeteo`); declare API keys in `local.properties` as `breezy.<sourceid>.key`; source identifier referenced in `app/build.gradle.kts` as `BuildConfig` field

---

*Convention analysis: 2026-05-13*
