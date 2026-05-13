# Technical Concerns

## Critical (Blockers / Security)

- **`singature/` folder contains sensitive key material** — `singature/key_text.txt`, `singature/secret.txt`, and the `singature/sinatrue` binary contain signing key data. These files are in the repository directory and must not be checked into VCS or exposed. The folder name is a typo of "signature".
  - Files: `singature/key_text.txt`, `singature/secret.txt`, `singature/sinatrue`, `singature/output/`
- **API keys are compile-time build config injected via `local.properties`** — Keys for AccuWeather, Météo-France, OpenWeatherMap, etc. are baked into the APK at build time via `BuildConfig`. This is the standard Android pattern, but keys embedded in release APKs can be extracted via reverse engineering. The official release mitigates this with obfuscation (R8/ProGuard).

## High Priority (Tech Debt)

- **AGP 9 upgrade blockers (3 TODOs in `gradle.properties`):**
  - `android.dependency.useConstraints=true` — needs review for AGP 9 compatibility
  - `android.r8.strictFullModeForKeepRules=false` — strict mode is disabled; rules need audit
  - `android.r8.optimizedResourceShrinking=false` — disabled because weather icon reflection access is uncertain; a `package.keep.xml` was added but not confirmed working
  - File: `gradle.properties` lines 21–30
- **`ConfigStore` uses SharedPreferences, not DataStore** — two TODOs note it should be migrated to Android DataStore and made read-only when extensions land
  - File: `app/src/main/kotlin/org/breezyweather/domain/settings/ConfigStore.kt` lines 22–24
- **Timezone handling in `Astro.kt` is acknowledged as wrong** — `TODO: Works but the way timezones are handled is wrong` in sun/moon rise/set calculations
  - File: `app/src/main/kotlin/org/breezyweather/domain/weather/model/Astro.kt` line 32
- **Notification timezone bug** — `FIXME: Timezone` in notification rendering
  - File: `app/src/main/kotlin/org/breezyweather/remoteviews/Notifications.kt` line 256
- **`WeatherUpdateJob` missing feature** — `TODO: Implement this, it's a good idea` for an unimplemented update optimization
  - File: `app/src/main/kotlin/org/breezyweather/background/weather/WeatherUpdateJob.kt` line 236
- **Wallpaper service loads location from DB inefficiently** — `TODO: Isn't there a more efficient way than reloading the location from database` and a second bare TODO
  - File: `app/src/main/kotlin/org/breezyweather/wallpaper/MaterialLiveWallpaperService.kt` lines 360–362

## Medium Priority (Code Quality)

- **Pollen index thresholds incomplete** — 13+ pollen types (chestnut, cypress, hazel, hornbeam, linden, plane, plantain, poplar, sorrel, urticaceae, etc.) have placeholder threshold arrays `listOf(0, 1, 2, 3, 4)` with `// TODO` comments indicating real calibration data is missing
  - File: `app/src/main/kotlin/org/breezyweather/domain/weather/index/PollenIndex.kt` lines 37–61
- **`BreezyTimeZoneService` incomplete territory mappings** — several territories use `"GMT"` as a stub with `// TODO` noting the correct timezone IDs (e.g., French Southern Territories `"TF"`, US Minor Outlying Islands `"UM"`)
  - File: `app/src/main/kotlin/org/breezyweather/sources/breezytz/BreezyTimeZoneService.kt` lines 296, 312
- **Deprecated utility functions still present** — `UnitUtils.kt` exposes two `@Deprecated("Use Number.format() extension")` methods; call sites should be migrated
  - File: `app/src/main/kotlin/org/breezyweather/common/utils/UnitUtils.kt` lines 82, 98
- **Keyboard IME resize workaround** — `KeyboardResizeBugWorkaround` private class exists to work around a layout resize bug when the soft keyboard opens; this is a fragile workaround
  - File: `app/src/main/kotlin/org/breezyweather/remoteviews/config/AbstractWidgetConfigActivity.kt` line 122
- **Widget item icon spacing TODO** — `TODO: Shouldn't we let some space here regardless of whether there is an icon?`
  - File: `app/src/main/kotlin/org/breezyweather/remoteviews/trend/WidgetItemView.kt` line 109

## Low Priority (Cosmetic / Minor)

- **Typo in folder name**: `singature/` should be `signature/` — affects any scripts referencing this path
- **`gradle.parallel=false`** — parallel Gradle execution is disabled, slowing build times on multi-core machines
- **`org.gradle.caching=false`** — Gradle build caching is disabled; re-enabling could significantly speed up incremental builds

## Incomplete Features / TODOs

- **Pollen threshold calibration** — 13+ pollen types lack real-world calibration data for their index thresholds (see `PollenIndex.kt`)
- **Extensions architecture** — `ConfigStore.kt` TODOs reference a future "extensions" system that is not yet implemented
- **WeatherUpdateJob optimization** — an unimplemented background refresh optimization noted in `WeatherUpdateJob.kt`
- **Full timezone mapping** — `BreezyTimeZoneService` has incomplete country-to-timezone mappings for some territories

## Dependency Concerns

- **AGP 9 compatibility** — three `gradle.properties` flags are suppressing strict AGP 9 behaviors; a proper AGP 9 migration is pending
- **RxJava 3 at source boundary** — RxJava 3 is still used at weather/geocoding source interfaces and bridged to Coroutines; this is a mixed-paradigm boundary that complicates maintenance
- Check `gradle/libs.versions.toml` for specific pinned versions; no obviously EOL dependencies identified from surface scan

## Security / Privacy Concerns

- **Signing artifacts in repo folder** — `singature/` contains what appear to be signing key fragments and utilities (`Algorithm.py`, `APK signer.py`, `key_text.txt`, `secret.txt`). These must never be committed to VCS.
- **API keys in `local.properties`** — correctly excluded from VCS (standard Android pattern), but developers must ensure `local.properties` is in `.gitignore`
- **No keys hardcoded in source** — API keys are injected via `BuildConfig` from `local.properties`/build args; no keys were found hardcoded in Kotlin source files

## Performance Concerns

- **`org.gradle.parallel=false`** — single-threaded Gradle execution; consider re-enabling with decoupled project checks
- **Wallpaper service DB reload** — acknowledged inefficiency in `MaterialLiveWallpaperService` (reloads location from DB unnecessarily)
- **Hybrid View/Compose UI** — the main weather screen uses a traditional RecyclerView + custom ViewHolders while settings/details screens use Compose; mixed rendering paradigm has overhead and maintenance cost

## Known Issues from Changelog (v6.2.0, 2026-05-01)

- **Race condition fix** — editing locations while weather is refreshing was forbidden to avoid a race condition (recently patched)
- **Address lookup regression** — address lookup broke after changing sources on `geo:` intent locations (recently patched)
- **Notification sensitivity bug** — some notifications were incorrectly marked sensitive (recently patched)
- **Notification-widget feels-like bug** — "use feels like" option was not always honored (recently patched)
- **Crash on 0% humidity** — crash when relative humidity is 0% and dewpoint is missing (recently patched)

## Observations

- **This is a fork of Breezy Weather** — the project links issue tracker and source code to `github.com/wikiepeidia/vn-weather-with-locationiq-and-nominatim`; the upstream is `breezy-weather/breezy-weather`. The central customization is the unified `NominatimService` (Nominatim + LocationIQ) replacing the upstream geocoding sources.
- **No unit tests found in a surface scan** — `app/src/test/` exists but test coverage appears minimal based on the test run command in `docs/TECHNICAL.md` (`testBasicDebugUnitTest`); see `TESTING.md` for detail
- **`singature/` Python scripts** — `Algorithm.py`, `APK signer.py`, `signatreverify.py` are custom signing utilities; their presence outside the standard Gradle signing pipeline is unusual and warrants review
