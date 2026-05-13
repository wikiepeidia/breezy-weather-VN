# Quick Task 260504-ccx: Rebase Fork Onto Latest Breezy Weather

**Date:** 2026-05-04
**Working branch:** `rebase/upstream-main-20260504`
**Backup branch:** `backup/fork-main-20260504`
**Upstream target:** `upstream/main` (`v6.2.0` + `New cycle` commit)

## Objective

Move the app onto the latest Breezy Weather upstream while preserving fork-specific behavior that users depend on:

- VN reverse-geocoding algorithm improvements
- LocationIQ support and recent LocationIQ recovery logic
- Widget refresh button behavior
- HyperOS/MIUI watchdog behavior
- Supporting settings, manifest, strings, notifications, and widget wiring required by the above

## Safety Rules

- Do not rewrite or force-push the existing `main` branch.
- Keep all integration work isolated on `rebase/upstream-main-20260504`.
- Treat `.env/` and `singature/` as unrelated untracked local content.
- Prefer `git cherry-pick --no-commit` for clean local commits.
- Extract only app files from mixed/noisy commits.

## Fork Slices To Preserve

### 1. VN algorithm / Nominatim

Primary local commits:

- `02c92fed3` structured VN ward fields
- `b6400cfe8` source-aware token extraction and VN cross-validation
- `481fe82eb` lazy Nominatim / reliability work
- `5297198d1` VN address unit tests
- `1eea129ad` ward pick ordering fix
- `0077e280f` display name parsing fix
- `bef29ec85` user-agent / isConfigured fix
- `ca511c3d1` free-source `isConfigured` fix
- `d5fd576de` non-ASCII user-agent cleanup

Notes:

- `709aad18a` also touches `NominatimService.kt`, but its source-priority hunk conflicts with later algorithm work and should not be replayed wholesale.

### 2. LocationIQ recovery

Primary local slices:

- Final `NominatimService.kt` and `NominatimServiceTest.kt` behavior from `backup/fork-main-20260504`
- `5230f91c8` minor cleanup on top of the recovery logic
- `ab0569194` contains the substantial recent LocationIQ recovery code, but it is a mixed commit and must be ported selectively

Supporting files:

- `app/src/main/java/org/breezyweather/sources/nominatim/NominatimApi.kt`
- `app/src/main/java/org/breezyweather/sources/nominatim/json/NominatimAddress.kt`
- `app/src/main/java/org/breezyweather/sources/nominatim/json/NominatimLocationResult.kt`
- `app/src/main/res/values/strings.xml`

### 3. Widget refresh button

Fork-only behavior. Not present in upstream.

Required files are all from mixed commit `709aad18a`:

- `app/src/main/java/org/breezyweather/background/receiver/widget/WidgetMaterialYouCurrentProvider.kt`
- `app/src/main/java/org/breezyweather/background/receiver/widget/WidgetMaterialYouForecastProvider.kt`
- `app/src/main/java/org/breezyweather/remoteviews/presenters/MaterialYouCurrentWidgetIMP.kt`
- `app/src/main/java/org/breezyweather/remoteviews/presenters/MaterialYouForecastWidgetIMP.kt`
- `app/src/main/java/org/breezyweather/ui/main/MainActivityViewModel.kt`
- `app/src/main/res/layout/widget_material_you_current.xml`
- `app/src/main/res/layout/widget_material_you_forecast_2x2.xml`
- `app/src/main/res/layout/widget_material_you_forecast_3x1.xml`
- `app/src/main/res/layout/widget_material_you_forecast_3x2.xml`
- `app/src/main/res/layout/widget_material_you_forecast_4x1.xml`
- `app/src/main/res/layout/widget_material_you_forecast_4x2.xml`
- `app/src/main/res/layout/widget_material_you_forecast_4x3.xml`
- `app/src/main/res/layout/widget_material_you_forecast_5x2.xml`
- `app/src/main/res/layout/widget_material_you_forecast_5x3.xml`

### 4. Watchdog / HyperOS resilience

Primary local commits:

- `3d7c34645` notification channel and ID
- `9b67f28d8` manifest wiring
- `c7185e2e2` core watchdog service and alarm receiver
- `30bd7a9da` settings and strings
- `9a7d33b53` settings UI section
- `250bc35e3` boot restart wiring
- `87ba3dcd2` alarm receiver guard and safer stop
- `304413bca` heartbeat interval slider
- `b00a58db9` notification enhancement
- `4281d8e9c` watchdog notification visibility toggle
- `dbff3c104` widget-notification piggyback
- `444a75016` avoid replacing weather notification
- `457544d65` later watchdog adjustments

Required file set:

- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/org/breezyweather/background/receiver/BootReceiver.kt`
- `app/src/main/java/org/breezyweather/background/watchdog/WatchdogAlarmReceiver.kt`
- `app/src/main/java/org/breezyweather/background/watchdog/WatchdogRestartWorker.kt`
- `app/src/main/java/org/breezyweather/background/watchdog/WatchdogService.kt`
- `app/src/main/java/org/breezyweather/domain/settings/SettingsManager.kt`
- `app/src/main/java/org/breezyweather/remoteviews/Notifications.kt`
- `app/src/main/java/org/breezyweather/ui/settings/compose/BackgroundUpdatesSettingsScreen.kt`
- `app/src/main/res/values/strings.xml`

## Execution Order

1. Replay clean VN / Nominatim commits onto the upstream branch without committing.
2. Port the final LocationIQ recovery slices selectively from the mixed April commits.
3. Port watchdog commits/slices.
4. Port widget refresh slices from `709aad18a`.
5. Run targeted tests and compile checks.
6. Write a summary of preserved behavior and remaining conflict/risk areas.

## Validation Plan

- `./gradlew :app:testDebugUnitTest --tests org.breezyweather.sources.nominatim.NominatimServiceTest`
- `./gradlew :app:compileDebugKotlin`
- Review resulting changed-file list to ensure only intended slices were pulled forward
