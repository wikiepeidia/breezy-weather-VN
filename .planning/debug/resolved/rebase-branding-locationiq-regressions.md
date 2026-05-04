---
status: resolved
trigger: "Investigate issue: rebase-branding-locationiq-regressions"
created: 2026-05-04T00:00:00Z
updated: 2026-05-04T03:14:06Z
---

## Current Focus

hypothesis: Confirmed. The rebase regression was the incomplete `app/src/res_fork` restoration; no separate missing LocationIQ-visible code path was found in the current search/settings slices.
test: Automated verification has been accepted as sufficient for finalization.
expecting: The archived session captures the resolved branding restore and the verified LocationIQ naming/summary behavior.
next_action: none; session archived. Only optional manual in-app visual confirmation remains advisable

## Symptoms

expected: The app name, launcher icon, and fork-specific licensed images/resources should match the fork on the rebased app in the relevant variants. When a valid LocationIQ `pk.*` key is configured for the Nominatim source, the source UI and/or behavior should reflect Nominatim / LocationIQ instead of behaving like plain Nominatim-only.
actual: The current upstream-based branch still appears to use incorrect/upstream branding or image resources instead of the fork's rebased branding/licensed assets. Separately, adding a LocationIQ API key or related setting appears to do nothing; the source label does not change to Nominatim / LocationIQ and the user reports no visible effect.
errors: No exact runtime error string was provided. `./gradlew assembleDebug` currently exits 0, so this is not a basic compile break. Prior targeted test/compile work on Nominatim and watchdog already succeeded. The user is unavailable for follow-up answers, so infer from the repo and current branch state.
reproduction: 1) Build/run the current integration branch and inspect app name, launcher icon, and licensed image/resource overlays across the active app variant(s). 2) Open the Nominatim source settings, enter a LocationIQ `pk.*` key and/or endpoint, save, then inspect whether the source label/config summary changes and whether the implementation actually uses the LocationIQ path.
started: These regressions exist on the current upstream rebase branch after porting the core fork slices. The fork branding/assets and LocationIQ behavior previously existed on the pre-rebase fork.

## Eliminated

- hypothesis: The active fork flavor is not loading `src/res_fork`, so upstream branding always wins.
 evidence: `app/build.gradle.kts` includes `src/res_fork` in both `basic` and `freenet` source sets whenever `Config.isBreezy` is false.
 timestamp: 2026-05-04T00:00:00Z

- hypothesis: The rebase dropped the LocationIQ-visible behavior directly inside `NominatimService`.
 evidence: Current `NominatimService` identity, search, reverse-geocoding, and preference-summary slices match the backup fork implementation after the upstream file move.
 timestamp: 2026-05-04T00:00:00Z

- hypothesis: `NominatimService` is not registered as a location-search source, so LocationIQ search can never be surfaced in the UI.
 evidence: `SourceManager.getLocationSearchSources()` now derives from `sourceList.filterIsInstance<LocationSearchSource>()`, so any `LocationSearchSource` implementation, including `NominatimService`, is already exposed.
 timestamp: 2026-05-04T00:00:00Z

- hypothesis: The remaining LocationIQ-visible regression is caused by the current search/settings label path hiding the Nominatim / LocationIQ identity or `pk.*` state.
 evidence: `SearchActivity` renders the active source from `SourceManager.getLocationSearchSourceOrDefault(...)` and displays each source with `it.getName(context)`; `LocationSettingsScreen` still uses each location source name in the selector summary; `NominatimService` still exposes `override val name = "Nominatim / LocationIQ"` and `pk.*` summaries such as `LocationIQ+Nominatim` and `Active endpoint:`.
 timestamp: 2026-05-04T03:07:42Z

## Evidence

- timestamp: 2026-05-04T00:00:00Z
 checked: .planning/quick/260504-ccx-rebase-fork-onto-latest-breezy-weather-w/260504-ccx-SUMMARY.md
 found: The rebase summary explicitly claims LocationIQ support and the configurable endpoint were ported, but it does not mention branding/assets.
 implication: Branding was likely out of scope for the previous port and must be verified separately.

- timestamp: 2026-05-04T00:00:00Z
 checked: app/build.gradle.kts sourceSets for basic/freenet
 found: Both fork flavors add `src/res_fork` when `Config.isBreezy` is false.
 implication: The branding regression is not caused by flavor/source-set exclusion.

- timestamp: 2026-05-04T00:00:00Z
 checked: app/src/res_fork/values/strings.xml and `git show backup/fork-main-20260504:app/src/res_fork/values/strings.xml`
 found: Current branch sets `brand_name` to generic `No Name Weather`, while the backup fork sets it to `Weather VN`.
 implication: The current `res_fork` overlay was not fully rebased from the fork and is a direct branding regression.

- timestamp: 2026-05-04T00:00:00Z
 checked: `git diff --name-status backup/fork-main-20260504..HEAD -- app/src/res_fork`
 found: Every launcher asset under `app/src/res_fork` differs from the backup fork, and `app/src/res_fork/icon_source.svg` is missing entirely.
 implication: The branding regression includes both strings and licensed image assets, not just an app-name string.

- timestamp: 2026-05-04T00:00:00Z
 checked: `git status --short -- app/src/res_fork`
 found: No local uncommitted changes are present under `app/src/res_fork`.
 implication: Restoring the fork overlay from the backup branch is safe and will not overwrite unrelated local work.

- timestamp: 2026-05-04T00:00:00Z
 checked: `git archive backup/fork-main-20260504 app/src/res_fork | tar -x`, followed by focused diff/status checks
 found: The fork overlay files were restored into the worktree; `brand_name` now matches the backup fork and `icon_source.svg` is present as a restored file.
 implication: The branding regression has a concrete worktree fix and no longer blocks the remaining LocationIQ investigation.

- timestamp: 2026-05-04T00:00:00Z
 checked: app/src/main/kotlin/org/breezyweather/sources/SourceManager.kt location-search accessors
 found: The current code no longer uses the earlier hard-coded `locationSearchSourceList` for retrieval; `getLocationSearchSources()` filters `sourceList` by `LocationSearchSource` instead.
 implication: Missing source registration is not the reason the UI fails to reflect LocationIQ state.

- timestamp: 2026-05-04T00:00:00Z
 checked: app/src/main/kotlin/org/breezyweather/sources/nominatim/NominatimService.kt versus backup fork service file
 found: The service still uses the combined `Nominatim / LocationIQ` name and exposes preference summaries like `LocationIQ+Nominatim` and `Active endpoint:` when a `pk.*` key is present.
 implication: The visible LocationIQ problem is not explained by a missing backend/service implementation in the rebased branch.

- timestamp: 2026-05-04T03:07:42Z
 checked: app/src/main/kotlin/org/breezyweather/ui/search/SearchActivity.kt, SearchViewModel.kt, SearchActivityRepository.kt, and app/src/main/kotlin/org/breezyweather/ui/settings/compose/LocationSettingsScreen.kt
 found: The search view-model seeds its state from `repository.lastSelectedLocationSearchSource`, the repository already prefers `nominatim` when a `pk.*` key is configured and the stored source is unset/default, and the search/settings selectors display the source via `getName(context)` or `name`, which for `NominatimService` is still `Nominatim / LocationIQ`.
 implication: The current search/settings state path already exposes the fork's LocationIQ-visible naming; no missing selection-label port has been found in these UI slices.

- timestamp: 2026-05-04T03:07:42Z
 checked: `git diff --stat backup/fork-main-20260504 -- app/src/res_fork` and `git status --short -- app/src/res_fork`
 found: The restored branding overlay is present in the worktree, but `icon_source.svg` is currently untracked rather than part of the branch snapshot, so a plain branch diff still reports it as deleted even though the file now exists locally.
 implication: Branding restoration looks substantially correct, but backup-parity must be verified against actual directory contents, not just tracked-file diff output.

- timestamp: 2026-05-04T03:11:36Z
 checked: `git archive backup/fork-main-20260504 app/src/res_fork | tar -x` to a temp dir, then `diff -qr` against the working `app/src/res_fork`
 found: The command produced no output, meaning the restored working `app/src/res_fork` directory matches the backup fork snapshot exactly when untracked files like `icon_source.svg` are included.
 implication: The branding restoration is valid and should be kept as the fix for the fork-overlay regression.

- timestamp: 2026-05-04T03:11:36Z
 checked: `./gradlew --no-daemon :app:testBasicDebugUnitTest --tests org.breezyweather.ui.search.SearchActivityRepositoryTest && ./gradlew --no-daemon assembleDebug`
 found: The focused location-search unit test passed and the full debug build succeeded after the branding restore, with only pre-existing warnings unrelated to the investigated regressions.
 implication: The restored fork assets compile cleanly, and the existing LocationIQ selection logic remains valid under executable verification.

## Resolution

root_cause: The rebase left the fork branding overlay under `app/src/res_fork` incomplete relative to the backup fork; this caused the branding regression. The remaining LocationIQ-visible-state report was not reproducible as a separate missing code path in the current search/settings slices, which still preserve the fork's Nominatim / LocationIQ display and `pk.*` summary behavior.
fix: Kept the restored `app/src/res_fork` overlay from the backup fork, including the launcher assets, fork strings, and `icon_source.svg`. No additional LocationIQ code change was applied because the current search/settings and repository paths already validate the intended visible-state behavior.
verification: `diff -qr` against an archived backup snapshot reported no differences for `app/src/res_fork`; `./gradlew --no-daemon :app:testBasicDebugUnitTest --tests org.breezyweather.ui.search.SearchActivityRepositoryTest` passed; `./gradlew --no-daemon assembleDebug` passed. Manual in-app visual confirmation of branding is still advisable, but no remaining automated failure is known.
files_changed: ["app/src/res_fork"]
