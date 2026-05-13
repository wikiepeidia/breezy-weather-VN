# Testing

**Analysis Date:** 2026-05-13

## Test Strategy

This project uses **JVM unit tests only**. No instrumented (`androidTest`) tests are present. Tests exercise pure Kotlin/Java logic — UI logic, option parsing, converter functions, and source-specific helper methods — with Android framework dependencies mocked via MockK.

Coverage enforcement is not configured. The CI pipeline does **not** run `./gradlew test` automatically (it runs lint + assembly only); tests are intended to be run locally before submitting a PR.

## Test Locations

| Module | Test Type | Path |
|--------|-----------|------|
| `app` | JVM unit (Kotlin source set) | `app/src/test/kotlin/org/breezyweather/` |
| `app` | JVM unit (Java source set, Kotlin files) | `app/src/test/java/org/breezyweather/` |
| `data` | — | No test files present |
| `domain` | — | No test files present |
| `maps-utils` | — | No test files present |
| `ui-weather-view` | — | No test files present |
| `weather-unit` | — | No test files present |

> Note: `app/src/test/java/` uses a Java directory layout but contains Kotlin source files. Prefer `app/src/test/kotlin/` for new tests to stay consistent with the majority.

## Test Frameworks

| Library | Version | Role |
|---------|---------|------|
| JUnit Jupiter (`org.junit.jupiter:junit-jupiter`) | 6.0.3 | Test runner (JUnit 5) |
| `org.junit.platform:junit-platform-launcher` | — | Platform launcher for JUnit 5 |
| Kotest Assertions (`io.kotest:kotest-assertions-core`) | 6.1.11 | Fluent infix assertions (`shouldBe`) |
| MockK (`io.mockk:mockk`) | 1.14.9 | Mocking/stubbing of Android and project classes |
| `kotlinx-coroutines-test` | 1.10.2 | `runTest` coroutine test scope |

All four are declared as a bundle in `gradle/libs.versions.toml`:

```toml
test = ["junit", "kotest-assertions", "kotlinx-coroutines-test", "mockk"]
```

**JUnit 5 platform activation** is applied globally by `configureTest()` in `buildSrc/src/main/kotlin/breezy/buildlogic/ProjectExtensions.kt`:

```kotlin
internal fun Project.configureTest() {
    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        }
    }
}
```

This is called from `breezy.android.application.gradle.kts` and `breezy.library.gradle.kts`, so all modules share the same test configuration.

## Run Commands

```bash
./gradlew test                # Run all unit tests (all modules)
./gradlew :app:test           # Run app-module tests only
./gradlew :app:testDebugUnitTest  # Run debug variant tests
```

## Coverage Areas

Tests currently exercise:

| Area | Files |
|------|-------|
| Wind direction converter | `app/src/test/kotlin/.../sources/CommonConverterTest.kt` |
| `CardDisplay` serialization/deserialization | `app/src/test/kotlin/.../option/appearance/CardDisplayTest.kt` |
| `DailyTrendDisplay` serialization/deserialization | `app/src/test/kotlin/.../option/appearance/DailyTrendDisplayTest.kt` |
| `UnitUtils.getNameByValue` | `app/src/test/kotlin/.../option/utils/UtilsTest.kt` |
| `SearchActivityRepository.resolveLocationSearchSource` | `app/src/test/java/.../ui/search/SearchActivityRepositoryTest.kt` |
| `NominatimService` VN address logic and key detection | `app/src/test/java/.../sources/nominatim/NominatimServiceTest.kt` |
| Stub placeholder | `app/src/test/kotlin/.../LocationTest.kt` (empty class, no tests) |
| String split exploratory | `app/src/test/kotlin/.../MatchTest.kt` (print-only test) |

## Test Patterns

### Basic JUnit 5 + Kotest assertion

```kotlin
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class CommonConverterTest {
    @Test
    fun getWindDegreeTest() = runTest {
        getWindDegree(null) shouldBe null
        getWindDegree("E") shouldBe 90.0
    }
}
```

All tests use `= runTest { }` body even when testing synchronous code. This is the consistent project-wide pattern.

### Backtick test names (descriptive)

Used in `SearchActivityRepositoryTest` and `NominatimServiceTest` for complex business-rule tests:

```kotlin
@Test
fun `resolveLocationSearchSource prefers nominatim when locationiq key exists and source is unset`() {
    SearchActivityRepository.resolveLocationSearchSource(
        storedSource = null,
        defaultSource = "openmeteo",
        hasLocationIqKey = true,
    ) shouldBe "nominatim"
}
```

Use backtick names when the test verifies a specific behavioral rule that benefits from a plain-English description.

### Mocking Android framework with MockK

```kotlin
import io.mockk.every
import io.mockk.mockk

val context = mockk<Context>().apply {
    every { getString(any()) } returns "Name"
    every { getString(org.breezyweather.unit.R.string.locale_separator) } returns ", "
}
```

Used in `CardDisplayTest` and `DailyTrendDisplayTest` to avoid requiring an Android runtime. MockK's `every { } returns` DSL is the project's stubbing idiom. No `verify` calls are present in current tests — assertions are done with `shouldBe`.

### MockK for Resources

```kotlin
val res = mockk<Resources>()
every { res.getStringArray(R.array.dark_modes) } returns
    arrayOf("Automatic", "Follow system", "Always light", "Always dark")
every { res.getStringArray(R.array.dark_mode_values) } returns arrayOf("auto", "system", "light", "dark")
```

(`app/src/test/kotlin/org/breezyweather/option/utils/UtilsTest.kt`)

### JSON deserialization test with `kotlinx.serialization`

`NominatimServiceTest` constructs `Json {}` directly and deserializes test payloads to verify JSON model mapping:

```kotlin
private val json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}
```

No file-based fixture loading — test JSON strings are created inline.

### Helper builder in test class

`NominatimServiceTest` uses a private helper to reduce repetition:

```kotlin
private fun addressOf(city: String?, countryCode: String = "vn") = LocationAddressInfo(
    countryCode = countryCode,
    city = city,
)
```

### HTTP error simulation with Retrofit

```kotlin
private fun httpException(code: Int, message: String): HttpException {
    val response = Response.error<String>(
        code,
        message.toResponseBody("text/plain".toMediaType()),
    )
    return HttpException(response)
}
```

(`app/src/test/java/.../sources/nominatim/NominatimServiceTest.kt`)

## Notable Test Files

| File | What it tests |
|------|--------------|
| `app/src/test/java/.../sources/nominatim/NominatimServiceTest.kt` | Most comprehensive: VN address parsing, LocationIQ key detection, HTTP error handling, JSON deserialization — backtick-named tests |
| `app/src/test/java/.../ui/search/SearchActivityRepositoryTest.kt` | `resolveLocationSearchSource` routing logic — 4 clearly named behavioral tests |
| `app/src/test/kotlin/.../option/appearance/CardDisplayTest.kt` | Round-trip serialization of `CardDisplay` enum list ↔ `&`-delimited string |
| `app/src/test/kotlin/.../option/appearance/DailyTrendDisplayTest.kt` | Same round-trip pattern for `DailyTrendDisplay` |
| `app/src/test/kotlin/.../option/utils/UtilsTest.kt` | `UnitUtils.getNameByValue` with mocked `Resources` |
| `app/src/test/kotlin/.../sources/CommonConverterTest.kt` | Wind direction string → degree conversion |
| `app/src/test/kotlin/.../LocationTest.kt` | **Empty stub** — declares the class with no test methods |
| `app/src/test/kotlin/.../MatchTest.kt` | Exploratory print test — not a real assertion test |

## CI/CD Integration

**GitHub Actions workflows** (`.github/workflows/`):

| Workflow | Trigger | Test steps |
|----------|---------|-----------|
| `push.yml` | Push to `main`, version tags | `spotlessCheck` + assembly only — **no `test` task** |
| `pull-request.yml` | PR to `main` or `dev` | `spotlessCheck` + `assembleBasicDebug` — **no `test` task** |

**Tests are not run in CI.** They must be run manually before submitting:

```bash
./gradlew test
```

**Linting is run in CI:**

```bash
./gradlew spotlessCheck
```

CI uses Ubuntu 24.04, Temurin JDK (version from `.github/.java-version`), and Gradle via `gradle/actions/setup-gradle`.

**No coverage reporting** is configured (no Jacoco, no Kover, no coverage upload step).

---

*Testing analysis: 2026-05-13*
