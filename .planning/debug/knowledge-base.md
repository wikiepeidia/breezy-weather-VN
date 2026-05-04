# GSD Debug Knowledge Base

Resolved debug sessions. Used by `gsd-debugger` to surface known-pattern hypotheses at the start of new investigations.

---

## rebase-branding-locationiq-regressions — Rebase missed the full fork branding overlay while LocationIQ UI behavior remained intact

- **Date:** 2026-05-04
- **Error patterns:** incorrect branding, upstream resources, LocationIQ key appears to do nothing, Nominatim / LocationIQ label, no visible effect
- **Root cause:** The rebase left the fork branding overlay under `app/src/res_fork` incomplete relative to the backup fork; this caused the branding regression. The remaining LocationIQ-visible-state report was not reproducible as a separate missing code path in the current search/settings slices, which still preserve the fork's Nominatim / LocationIQ display and `pk.*` summary behavior.
- **Fix:** Kept the restored `app/src/res_fork` overlay from the backup fork, including the launcher assets, fork strings, and `icon_source.svg`. No additional LocationIQ code change was applied because the current search/settings and repository paths already validate the intended visible-state behavior.
- **Files changed:** app/src/res_fork

---

## locationiq-key-not-recognized — Whitespace around persisted `pk.*` keys prevented LocationIQ detection

- **Date:** 2026-05-04
- **Error patterns:** LocationIQ key not recognized, Nominatim only, pk key saved but ignored, silent issue, whitespace wrapped key
- **Root cause:** NominatimService stored and consumed the Nominatim / LocationIQ `instance` value as raw text, so a valid `pk.*` key with surrounding whitespace could be persisted and reopened but still fail the summary/runtime LocationIQ gate because `isLocationIqKey` required an exact untrimmed `pk.` prefix.
- **Fix:** Trimmed the persisted Nominatim / LocationIQ `instance` value before reuse and persistence, removed default-base values at the setter, updated `isLocationIqKey` to trim before checking `pk.`, and added regression tests for whitespace-wrapped keys.
- **Files changed:** app/src/main/kotlin/org/breezyweather/sources/nominatim/NominatimService.kt, app/src/test/java/org/breezyweather/sources/nominatim/NominatimServiceTest.kt

---
