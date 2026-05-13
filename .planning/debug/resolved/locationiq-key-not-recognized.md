---
status: resolved
trigger: "locationiq-key-not-recognized"
created: 2026-05-04T10:27:56+07:00
updated: 2026-05-04T10:44:31+07:00
---

## Current Focus

hypothesis: Confirmed. The Nominatim / LocationIQ path was reading the correct preference, but it treated the saved `instance` value as raw text. Whitespace-wrapped `pk.*` keys persisted visibly yet failed the summary/runtime LocationIQ gate because NominatimService only recognized an exact untrimmed `pk.` prefix.
test: Re-run the focused NominatimService regression suite against the current tree and confirm the normalization fix still passes.
expecting: If the fix remains intact, the whitespace-wrapped `pk.*` regression coverage should pass again because the service now trims stored values before classification and persistence.
next_action: None. Session archived; manual in-app settings verification remains optional residual validation.

## Symptoms

expected: After entering a valid pk.*LocationIQ key in the Nominatim settings, the UI should no longer behave like plain Nominatim-only. The summary should reflect LocationIQ usage, such as LocationIQ+Nominatim or similar fork behavior, and the source should be treated as Nominatim / LocationIQ.
actual: The key stays saved in the setting field when reopened, but the UI still says Nominatim only and no visible LocationIQ recognition is triggered.
errors: Silent issue. No error message was reported.
reproduction: Open the Nominatim / LocationIQ setting, enter a valid pk.* key, save, then inspect the setting summary/state. It still reports Nominatim only.
started: This worked on the old fork. It does not work on the rebased branch.

## Eliminated

## Evidence

- timestamp: 2026-05-04T10:27:56+07:00
 checked: Initial code search for the exact summary text and LocationIQ key handling.
 found: The literal "Nominatim only" summary branch and the "LocationIQ+Nominatim" branch both live in NominatimService settings code, while SearchActivityRepository separately derives hasLocationIqKey from the configured Nominatim instance.
 implication: The bug is likely in the local state/control path around the Nominatim instance preference or a rebase-induced preference-storage mismatch, not in a distant UI label file.

- timestamp: 2026-05-04T10:27:56+07:00
 checked: NominatimService settings block and SearchActivityRepository source-selection helper.
 found: NominatimService writes and reads the same `instance` preference via SourceConfigStore, and SearchActivityRepository also reads the same `instance` key. The repository trims and ignores case for `pk.` detection, but NominatimService summary uses `isLocationIqKey(content)` directly.
 implication: A pure write/read preference mismatch is less likely; the stricter summary-side key classification or the UI value passed into the summary lambda is now the most discriminating next check.

- timestamp: 2026-05-04T10:27:56+07:00
 checked: EditTextPreference composable, LocationSettingsScreen, WeatherSourcesSettingsScreen, and SourceConfigStore.
 found: The outer settings screens call `getPreferences(context)` on each composition and pass the model content directly into `EditTextPreferenceViewWithCard`; the preference storage file name also stays stable as `source_nominatim_preferences`.
 implication: A package/applicationId migration issue or a frozen outer preference list does not explain the stale `Nominatim only` summary; the remaining narrow seam is saved-value normalization inside NominatimService itself.

- timestamp: 2026-05-04T10:40:57+07:00
 checked: Focused `NominatimServiceTest` run with new whitespace-wrapped key regression tests.
 found: The new `isLocationIqKey - surrounding whitespace is ignored` and `classifyLocationIqKeyState - whitespace wrapped key detected as valid` tests both failed under the existing code.
 implication: The summary/runtime gate was misclassifying saved keys based on raw input formatting, which directly explains a persisted key still showing `Nominatim only`.

- timestamp: 2026-05-04T10:40:57+07:00
 checked: NominatimService normalization fix plus rerun of `./gradlew --no-daemon :app:testBasicDebugUnitTest --tests org.breezyweather.sources.nominatim.NominatimServiceTest`.
 found: The service now trims stored `instance` values before persistence/use, trims the `pk.` gate in `isLocationIqKey`, and the focused NominatimService test class passes.
 implication: The fix addresses the root normalization seam used by the settings summary and the actual LocationIQ request path.

- timestamp: 2026-05-04T10:44:31+07:00
 checked: Fresh rerun of `./gradlew --no-daemon :app:testBasicDebugUnitTest --tests org.breezyweather.sources.nominatim.NominatimServiceTest` during session finalization.
 found: Gradle reported `BUILD SUCCESSFUL`; the focused `NominatimServiceTest` suite still passes with the whitespace-wrapped key regression coverage in place.
 implication: Automated verification still confirms the recorded fix is present in the current tree, so the session can be closed without blocking on manual in-app retesting.

## Resolution

root_cause: NominatimService stored and consumed the Nominatim/LocationIQ `instance` value as raw text, so a valid `pk.*` key with surrounding whitespace could be persisted and reopened but still fail the summary/runtime LocationIQ gate because `isLocationIqKey` required an exact untrimmed `pk.` prefix.
fix: Normalized the stored `instance` value in NominatimService by trimming before persistence and reuse, removed default-base values at the setter, and updated `isLocationIqKey` to trim before checking the `pk.` prefix. Added regression tests covering whitespace-wrapped keys.
verification: Added focused regression tests for whitespace-wrapped `pk.*` keys and verified them with `./gradlew --no-daemon :app:testBasicDebugUnitTest --tests org.breezyweather.sources.nominatim.NominatimServiceTest` (passed). Re-ran the same focused suite during finalization on 2026-05-04T10:44:31+07:00 and it passed again. Manual in-app settings verification remains advisable but is not required to close this session.
files_changed: ["app/src/main/kotlin/org/breezyweather/sources/nominatim/NominatimService.kt", "app/src/test/java/org/breezyweather/sources/nominatim/NominatimServiceTest.kt"]
