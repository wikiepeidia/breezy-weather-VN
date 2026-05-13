# Integrations

**Analysis Date:** 2026-05-13

## External APIs — Weather Data Sources

Sources are split by flavor. All network sources extend `HttpSource` and implement one or more of `WeatherSource`, `LocationSearchSource`, `ReverseGeocodingSource`, `TimeZoneSource`, etc.

### Free Network Sources (`freenet` + `basic` flavors)

| API | Source ID | Auth | Base URL | Notes |
|-----|-----------|------|----------|-------|
| Open-Meteo Forecast | `openmeteo` | None | `https://api.open-meteo.com/` | Default weather source; also air quality via `https://air-quality-api.open-meteo.com/` |
| Open-Meteo Geocoding | `openmeteo` | None | `https://geocoding-api.open-meteo.com/` | Default location search source |
| BrightSky (DWD) | `brightsky` | None (configurable instance) | `https://api.brightsky.dev/` | Germany; configurable alternate instance URL |
| Nominatim / LocationIQ | `nominatim` | Optional API key (`pk.xxxx` → LocationIQ) | Nominatim: `https://nominatim.openstreetmap.org/` / LocationIQ: `https://us1.locationiq.com/v1/` | Unified service; key enables LocationIQ with endpoint fallback |
| MET.no | `metno` | None (User-Agent required) | `https://api.met.no/` | Norway national service (stub in main, implemented in freenet) |
| BMKG | `bmkg` | Optional key | — | Indonesia |
| BMD | `bmd` | None | — | Bangladesh |
| ClimWeb-based sources | Various | None | Various national WMO ClimWeb instances | Covers ~16 African national met services: AnamBF, Anamet, Dccms, DmnNe, DwrGm, EthioMet, GMet, Igebu, Inmgb, MaliMeteo, MeteoBenin, MeteoTchad, Mettelsat, MsdZw, SmaSc, SmaSu, Ssms |
| Natural Earth | `naturalearth` | None — offline | N/A (bundled GeoJSON) | Default reverse geocoding; data from `app/work/ne_50m_admin_0_countries.json` |
| BreezyTimeZone | `breezytz` | None — offline | N/A (bundled GeoJSON raw resources) | Offline timezone resolution; based on tzdb 2025b |
| Android Geocoder | `androidgeocoder` | None | System geocoder | Falls back to system geocoder |
| WMO Severe Weather | `wmosevereweather` | None | — | WMO global alerts |
| Debug source | `debug` | None | N/A | Development/testing only |

### Non-Free Network Sources (`basic` flavor only)

All keys defined via `local.properties` and injected as `BuildConfig` fields.

| API | Source ID | Auth / BuildConfig Key | Notes |
|-----|-----------|------------------------|-------|
| AccuWeather | `accu` | `ACCU_WEATHER_KEY` (`breezy.accu.key`) | Forecast, alerts, air quality |
| AEMET | `aemet` | `AEMET_KEY` (`breezy.aemet.key`) | Spain national met |
| Atmo AURA | `atmoaura` | `ATMO_AURA_KEY` | France air quality (Auvergne-Rhône-Alpes) |
| Atmo France | `atmofrance` | `ATMO_FRANCE_KEY` | France air quality (national) |
| Atmo Grand Est | `atmograndest` | `ATMO_GRAND_EST_KEY` | France air quality |
| Atmo HDF | `atmohdf` | `ATMO_HDF_KEY` | France air quality |
| Atmo Sud | `atmosud` | `ATMO_SUD_KEY` | France air quality |
| Baidu IP Location | `baiduip` | `BAIDU_IP_LOCATION_AK` (`breezy.baiduip.key`) | China IP-based location |
| CWA | `cwa` | `CWA_KEY` (`breezy.cwa.key`) | Taiwan Central Weather Administration |
| DMI | `dmi` | None required | Denmark |
| ECCC | `eccc` | `ECCC_KEY` (`breezy.eccc.key`) | Canada Environment and Climate Change |
| EKUK | `ekuk` | None | Estonia |
| EPDHK | `epdhk` | None | Hong Kong air quality |
| FMI | `fmi` | None | Finland |
| GeoNames | `geonames` | `GEO_NAMES_KEY` (`breezy.geonames.key`) | Location search / reverse geocoding |
| GeosphereAT | `geosphereat` | None | Austria |
| HKO | `hko` | None | Hong Kong Observatory |
| Ilmateenistus | `ilmateenistus` | None | Estonia |
| IMD | `imd` | None | India Meteorological Department |
| IMS | `ims` | None | Israel |
| Infoplaza | `infoplaza` | `INFOPLAZA_KEY` (`breezy.infoplaza.key`) | Netherlands |
| IPMA | `ipma` | None | Portugal |
| IPSB | `ipsb` | None | — |
| JMA | `jma` | None | Japan Meteorological Agency |
| KNMI | `knmi` | None | Netherlands |
| LHMT | `lhmt` | None | Lithuania |
| LVGMC | `lvgmc` | None | Latvia |
| MeteoAM | `meteoam` | None | Italy |
| MeteoLux | `meteolux` | None | Luxembourg |
| Met.ie | `metie` | `MET_IE_KEY` (`breezy.metie.key`) | Ireland |
| Met Office | `metoffice` | `MET_OFFICE_KEY` (`breezy.metoffice.key`) | UK |
| Météo-France | `mf` | `MF_WSFT_KEY` + `MF_WSFT_JWT_KEY` | JWT-signed requests via jjwt 0.13.0 |
| MGM | `mgm` | None | Turkey |
| NAMEM | `namem` | None | Mongolia |
| NCDR | `ncdr` | None | Taiwan |
| NCEI | `ncei` | None | USA historical data |
| NLSC | `nlsc` | None | Taiwan |
| NWS | `nws` | None | USA National Weather Service |
| OpenWeather | `openweather` | `OPEN_WEATHER_KEY` (`breezy.openweather.key`) | Global |
| PAGASA | `pagasa` | None | Philippines |
| Pirate Weather | `pirateweather` | `PIRATE_WEATHER_KEY` (`breezy.pirateweather.key`) | Dark Sky-compatible |
| Polleninfo | `polleninfo` | `POLLENINFO_KEY` (`breezy.polleninfo.key`) | Pollen data |
| Recosante | `recosante` | None | France air quality + pollen |
| SMG | `smg` | None | Macao |
| SMHI | `smhi` | None | Sweden |
| Veduris | `veduris` | None | — |

## Location Services

| Service | Source ID | Type | Notes |
|---------|-----------|------|-------|
| Android native location | `native` | Device GPS / Network / Fused | Default; uses `LocationManager` with fused provider on API 31+; implemented in `app/src/main/kotlin/org/breezyweather/sources/android/AndroidLocationService.kt` |
| Baidu IP Location | `baiduip` | IP-based (China) | Requires `BAIDU_IP_LOCATION_AK`; basic flavor only |
| Android Geocoder | `androidgeocoder` | System API | Fallback reverse geocoding |

## Geocoding / Location Search

| Service | Type | Auth | Notes |
|---------|------|------|-------|
| Open-Meteo Geocoding | HTTP | None | Default location search (`DEFAULT_LOCATION_SEARCH_SOURCE=openmeteo`) |
| Nominatim | HTTP | None (standard OSM instance) | `https://nominatim.openstreetmap.org/search` |
| LocationIQ | HTTP | API key (`pk.xxxx`) | Uses same `NominatimService`; key enables search; endpoint fallback chain (`us1` → `eu1` → `ap1`) |
| GeoNames | HTTP | `GEO_NAMES_KEY` | basic flavor only |
| Natural Earth | Offline | None | Default reverse geocoding (`DEFAULT_GEOCODING_SOURCE=naturalearth`); GeoJSON bundled in `app/work/ne_50m_admin_0_countries.json` |
| BreezyTimeZone | Offline | None | Timezone resolution from coordinate; GeoJSON as raw resources in `app/src/main/res/raw/breezytz_*.json` |

`NominatimService` unified implementation: `app/src/main/kotlin/org/breezyweather/sources/nominatim/NominatimService.kt`

## Data Sharing / Broadcast Integrations

| Integration | Direction | Protocol | Notes |
|-------------|-----------|----------|-------|
| GadgetBridge | Outgoing | Android broadcast (`nodomain.freeyourgadget.gadgetbridge.ACTION_GENERIC_WEATHER`) | Sends weather data to GadgetBridge for smart devices |
| Breezy Weather Data Sharing Lib | Outgoing | ContentProvider | `com.github.breezy-weather:breezy-weather-data-sharing-lib` (version `09c0e4dd`); exposes weather data to other apps via `${applicationId}.READ_PROVIDER` permission |
| Icon Packs | Incoming query | Intent (`org.breezyweather.ICON_PROVIDER`, `wangdaye.com.geometricweather.ICON_PROVIDER`) | Breezy Weather and Geometric Weather icon pack protocol |
| Chronus Icon Packs | Incoming query | `android.intent.action.MAIN` | Chronus-compatible icon packs |

## App Updates

| Service | Mechanism | Notes |
|---------|-----------|-------|
| GitHub Releases API | HTTPS (OkHttp) | Checks `https://api.github.com/repos/{GITHUB_ORG}/{GITHUB_REPO}/releases/latest`; opt-in via `isGitHubUpdateCheckerEnabled`; implemented in `app/src/main/kotlin/org/breezyweather/background/updater/` |

`GITHUB_ORG` and `GITHUB_REPO` injected from `gradle.properties` at build time.

## Analytics / Crash Reporting

None detected. No Firebase, Sentry, Crashlytics, or equivalent SDK is present.

## Third-party SDKs (non-UI)

| SDK | Version | Purpose | License |
|-----|---------|---------|---------|
| commons-suncalc | 2.14 | Astronomical calculations (sunrise, sunset, moon phase, twilight) | LGPL |
| jjwt | 0.13.0 | JWT creation/signing for Météo-France API (`basicImplementation` only) | Apache 2.0 |
| maps-utils (fork) | Local module | Google Maps Android Utils fork for GeoJSON parsing and polygon intersection; used by Natural Earth and BreezyTimeZone | Apache 2.0 |
| RestrictionBypass | `86d4e295` | Bypasses some network restrictions for specific sources | — |
| kotlinx-collections-immutable | 0.4.0 | Immutable collections for Compose stability | Apache 2.0 |
| AboutLibraries | 14.1.0 | Open-source license attribution screen in Settings | Apache 2.0 |

## Environment / Config

All API keys are sourced from `local.properties` (not committed to VCS) and embedded as `BuildConfig` string fields at compile time.

**Key properties in `local.properties`:**

```
# Default source overrides
breezy.source.default_location=native
breezy.source.default_location_search=openmeteo
breezy.source.default_geocoding=naturalearth
breezy.source.default_weather=auto

# API keys (all optional — sources degrade gracefully if absent)
breezy.accu.key=
breezy.aemet.key=
breezy.baiduip.key=
breezy.bmkg.key=
breezy.cwa.key=
breezy.eccc.key=
breezy.geonames.key=
breezy.metie.key=
breezy.metoffice.key=
breezy.mf.key=
breezy.mf.jwtKey=
breezy.openweather.key=
breezy.pirateweather.key=
breezy.polleninfo.key=
breezy.infoplaza.key=
breezy.atmoaura.key=
breezy.atmofrance.key=
breezy.atmograndest.key=
breezy.atmohdf.key=
breezy.atmosud.key=
```

**Key properties in `gradle.properties`:**

```
app.report_issue=          # Issue tracker URL or email
app.source_code_link=      # Source code URL (must start with https://)
app.releases_link=         # Releases page URL
app.install_instructions_link=
app.privacy_policy_link=   # Must start with https://
app.github.org=
app.github.repo=
app.github.release_prefix=
app.matrix_link=
app.icon_packs_link=
```

## Network Security

- `android:networkSecurityConfig="@xml/network_security_config"` enforced on the Application
- Let's Encrypt ISRG Root X1/X2 certificates manually pinned for API < 24 (injected via `okhttp-tls` in `HttpModule.kt`)
- No cleartext traffic expected (enforced by network security config)
- `RestrictionBypass` library (`com.github.breezy-weather:RestrictionBypass@86d4e295`) used for specific sources that require it

---

*Integration audit: 2026-05-13*
