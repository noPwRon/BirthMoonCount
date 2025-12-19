# GitHub Copilot / AI Agent Instructions for BirthMoonCount

This file gives focused, actionable guidance for AI coding agents working on this Android/Kotlin project.

1) Project overview
- Android app using Jetpack Compose for UI and Kotlin for astronomy logic.
- UI entry: `app/src/main/java/com/kimLunation/moon/MainActivity.kt` and `MoonScene()` compose tree.
- Astronomy logic: `app/src/main/java/com/kimLunation/moon/astronomy/*` (see `MoonPhase.kt`, `MoonFullMoonsMeeus.kt`).
- Image processing/rendering engines: `app/src/main/java/com/kimLunation/moon/ui/AstrolabeImageEngine.kt` and `MoonDiskEngine.kt`.
- Sensor input: `app/src/main/java/com/kimLunation/moon/PhoneTilt.kt` (provides `rememberPhoneRollDegrees`).

2) Big-picture architecture and flows
- Data flow: `MainActivity` (Compose) reads sensor state (`PhoneTilt`) and astronomy results (`MoonPhase` etc.) and passes values into composables and rendering engines.
- Separation of concerns: astronomy code lives under `astronomy/` as pure Kotlin utilities (stateless functions/objects). UI and image processing live under `ui/` and use Kotlin/Compose and Android Bitmap APIs.
- Global config: `KimConfig` is a Kotlin `object` singleton used for observer coordinates, timezone and birth instant.

3) Key files and examples (use these as starting points)
- Compose entry: `app/src/main/java/com/kimLunation/moon/MainActivity.kt` — look for `MoonScene()` which composes `MoonDiskEngine` and background images.
- Sensor helper: `PhoneTilt.kt` — shows how to register `SensorManager` and produce a Compose-friendly `State<Float>` for roll degrees.
- Astronomy util: `MoonPhase.kt` — use `MoonPhase.compute(Instant)` to get `PhaseResult(fraction, phaseName, ageDays)`; mirror its deterministic math when adding new astronomy features.
- Image processing: `AstrolabeImageEngine.loadProcessedAstrolabe(...)` — performs chroma-key/background removal on drawable bitmaps (use same `Dispatchers.IO` pattern for heavy image work).

4) Build, run, test workflows (Windows / PowerShell examples)
- Build debug APK: `.\\gradlew assembleDebug`
- Run unit tests: `.\\gradlew test` (runs `app/src/test` JVM tests)
- Run instrumented tests (device/emulator): `.\\gradlew connectedAndroidTest`
- Clean and rebuild: `.\\gradlew clean assembleDebug`
- Note: project uses Gradle Kotlin DSL and the wrapper; prefer `.\\gradlew` for consistent environment.

5) Project-specific conventions and patterns
- Kotlin singletons (`object`) are used for shared config/utilities (example: `KimConfig`, `MoonPhase`).
- Astronomy code favors deterministic, mean-element algorithms (see `MoonPhase.kt`) — prefer adding tests against known instants rather than relying on small floating discrepancies.
- Offload CPU/IO-heavy work to coroutines on `Dispatchers.IO` (see `AstrolabeImageEngine`).
- Compose state plumbing: prefer `remember` + `mutableStateOf` and provide small helpers like `rememberPhoneRollDegrees` rather than exposing raw Android APIs in composables.

6) Common edit patterns and safe changes
- If you change `KimConfig` timezone or birth instant, update any tests that rely on absolute `Instant` values.
- When adding new drawable assets referenced by code, ensure they are placed under `app/src/main/res/drawable*` and referenced by resource id (e.g., `R.drawable.astrolabe_ring`).
- For image processing adjustments, keep the background-removal heuristics (corner-key + brightness) consistent unless you add unit/image tests.

7) Integration points & external dependencies
- Version catalog: `gradle/libs.versions.toml` is used for dependency versions; update it when bumping libs.
- Proguard rules for release builds: `app/proguard-rules.pro`.
- No external network services are required for core calculations — most logic is local.

8) Where to add tests
- Unit-test pure astronomy functions under `app/src/test/java/com/kimLunation/moon/astronomy/` (compare `MoonPhase.compute` outputs for fixed `Instant`s).
- UI/unit tests: small Compose preview/tests can target `app/src/test` where possible; instrumented UI tests go under `app/src/androidTest`.

9) When in doubt
- Follow existing patterns: keep astronomical calculations pure and deterministic, image work on `Dispatchers.IO`, and UI state in Compose with `remember`.
- Refer to these files as canonical examples: `MainActivity.kt`, `PhoneTilt.kt`, `MoonPhase.kt`, `AstrolabeImageEngine.kt`, and `KimConfig.kt`.

If any section is unclear or you'd like more examples (e.g., typical unit tests for `MoonPhase` or a short recipe to add a new drawable and wire it into `MoonScene()`), tell me which area to expand.
