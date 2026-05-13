# Quick Task 260504-ccx: Integration Summary

**Date:** 2026-05-04
**Status:** runtime integration compile validated
**Working branch:** `rebase/upstream-main-20260504`
**Backup branch:** `backup/fork-main-20260504`
**Base:** `upstream/main`

## What Changed

This branch preserves the fork-specific behavior requested by the user while moving onto the latest Breezy Weather upstream codebase.

Preserved slices:

- VN reverse-geocoding parsing and merge behavior
- LocationIQ support and endpoint fallback recovery
- Widget refresh buttons and refresh broadcast handling
- Watchdog / HyperOS / MIUI keepalive behavior

## Integration Strategy

- Kept the existing fork `main` untouched.
- Created `backup/fork-main-20260504` as a rollback anchor.
- Built the integration on `rebase/upstream-main-20260504` from `upstream/main`.
- Avoided replaying mixed legacy fork commits wholesale where upstream file moves made cherry-picks unsafe.
- Ported only the required slices into current upstream Kotlin/Compose locations.

## Preserved Behavior Details

### 1. VN / Nominatim / LocationIQ

- Ported the fork's final `NominatimService` behavior into the upstream Kotlin source tree.
- Preserved VN-specific parsing helpers, best-subprovince selection, and merged-result behavior.
- Preserved LocationIQ key classification, endpoint normalization, candidate fallback handling, and retry flow.
- Added/ported supporting API and DTO updates required by the final service behavior.
- Added the missing string resource for the configurable LocationIQ endpoint.

Primary files:

- `app/src/main/kotlin/org/breezyweather/sources/nominatim/NominatimService.kt`
- `app/src/main/kotlin/org/breezyweather/sources/nominatim/NominatimApi.kt`
- `app/src/main/kotlin/org/breezyweather/sources/nominatim/json/NominatimAddress.kt`
- `app/src/main/kotlin/org/breezyweather/sources/nominatim/json/NominatimLocationResult.kt`
- `app/src/test/java/org/breezyweather/sources/nominatim/NominatimServiceTest.kt`
- `app/src/main/res/values/strings.xml`

### 2. Widget Refresh

- Added refresh action handling to current and forecast Material You widget providers.
- Added refresh pending intents to widget presenters.
- Added refresh button views to the Material You widget layouts.
- Preserved the user-triggered immediate refresh flow via `WeatherUpdateJob.startNow(...)`.
- Adjusted add-location reverse-geocoding selection to prefer Nominatim explicitly when available.

Primary files:

- `app/src/main/kotlin/org/breezyweather/background/receiver/widget/WidgetMaterialYouCurrentProvider.kt`
- `app/src/main/kotlin/org/breezyweather/background/receiver/widget/WidgetMaterialYouForecastProvider.kt`
- `app/src/main/kotlin/org/breezyweather/remoteviews/presenters/MaterialYouCurrentWidgetIMP.kt`
- `app/src/main/kotlin/org/breezyweather/remoteviews/presenters/MaterialYouForecastWidgetIMP.kt`
- `app/src/main/kotlin/org/breezyweather/ui/main/MainActivityViewModel.kt`
- Material You forecast/current widget layout XMLs

### 3. Watchdog / HyperOS Resilience

- Added watchdog service, alarm receiver, and periodic restart worker.
- Added boot-time restart wiring when watchdog is enabled.
- Added settings storage for watchdog enablement and heartbeat interval.
- Added watchdog notification channel and ID wiring.
- Added minimal settings UI for enabling the watchdog, battery optimization guidance, and Xiaomi/HyperOS autostart entry points.
- Added manifest wiring for foreground service, receiver, and wake lock.

Primary files:

- `app/src/main/kotlin/org/breezyweather/background/watchdog/WatchdogService.kt`
- `app/src/main/kotlin/org/breezyweather/background/watchdog/WatchdogAlarmReceiver.kt`
- `app/src/main/kotlin/org/breezyweather/background/watchdog/WatchdogRestartWorker.kt`
- `app/src/main/kotlin/org/breezyweather/background/receiver/BootReceiver.kt`
- `app/src/main/kotlin/org/breezyweather/domain/settings/SettingsManager.kt`
- `app/src/main/kotlin/org/breezyweather/remoteviews/Notifications.kt`
- `app/src/main/kotlin/org/breezyweather/ui/settings/compose/BackgroundUpdatesSettingsScreen.kt`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/res/values/strings.xml`

## Intentional Scope Decisions

- Did not overwrite the upstream background-updates settings screen with the entire older fork screen.
- Kept the watchdog UI minimal to preserve the required behavior without trampling unrelated upstream settings changes.
- Preserved the heartbeat interval setting backend for service scheduling, but did not reintroduce the full older watchdog dashboard/slider UX yet.
- Treated `.env/` and `singature/` as unrelated local content and left them untouched.

## Validation Performed

Executed successfully:

- `./gradlew --no-daemon :app:testBasicDebugUnitTest --tests org.breezyweather.sources.nominatim.NominatimServiceTest`
- `./gradlew --no-daemon :app:compileBasicDebugKotlin`

Notes:

- The Nominatim/LocationIQ regression suite passed.
- The integrated runtime app Kotlin sources compile successfully on the upstream-based branch.
- Build output still contains pre-existing project warnings about missing app metadata properties and Gradle/AGP performance suggestions; these were not changed as part of this task.

## Remaining Review Focus For Developers

- Decide whether the minimal watchdog UI is enough, or whether the older heartbeat slider / health dashboard needs a controlled follow-up port.
- Review the widget refresh UI placement on device to confirm the added buttons fit all supported widget sizes as intended.
- When ready, compare this branch against your fork `main` and upstream `main` for review/merge strategy, but do not rebase the old fork branch directly.
