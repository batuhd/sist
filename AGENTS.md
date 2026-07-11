# AGENTS.md — Sist

Compact guidance for working on this Android project.

## Project

- Android app written in **Kotlin 2.1.0**, **Jetpack Compose + Material 3**, **AGP 9.2.1**.
- Target SDK 36, min SDK 26. Compile SDK 36 with minor API level 1.
- App language is **Turkish**; user-facing strings are in Turkish.
- Repository: `https://github.com/batuhd/sist`.

## Build

```bash
./gradlew assembleDebug          # signed debug APK
./gradlew assembleRelease        # signed release APK (requires keystore config)
```

- Configuration cache is enabled (`org.gradle.configuration-cache=true`).
- Required property in `gradle.properties`: `android.disallowKotlinSourceSets=false` (allows KSP generated sources with built-in Kotlin).

## Architecture

- **MVVM** with **manual Dependency Injection** in `SistApplication.kt` → `AppContainer`.
- **No Hilt** — the AGP 9 DSL is incompatible with the old Hilt setup.
- Layers: `data/` (Room, remote API, repos), `domain/` (models, repo interfaces, use cases), `presentation/` (Compose screens + ViewModels).
- Entry point for UI: `MainActivity.kt` → `presentation.navigation.MainNavigation`.

## Database

- **Room** with **KSP** (`ksp` plugin). Entities in `data/local/entity/`, DAOs in `data/local/dao/`.
- `SistDatabase` uses `fallbackToDestructiveMigration()`: schema changes wipe user data in debug. Be careful with migrations; do not ship destructive migrations.

## Signing & Releases

- Release signing reads from `keystore.properties` and `release-key.jks` in the repo root.
- Both files are `.gitignore`d. If they are missing, `assembleRelease` builds an unsigned APK instead.
- To create a release:
  1. Bump `versionCode` and `versionName` in `app/build.gradle.kts`.
  2. Run `./gradlew assembleRelease`.
  3. Use `gh release create` to upload the APKs.

## Key Domain Specifics

- **Price sources**: Yahoo Finance for stocks/ETFs; FVT (Fintables) for Turkish investment funds (`FvtFundPriceProvider`).
- **Background work**: WorkManager workers in `worker/`. Workers are scheduled from `SistApplication.scheduleWorkers()`.
- **Widgets**: Glance App Widgets in `presentation/widget/`. Widget colors must use `ColorProvider(R.color.*)` from `res/values/widget_colors.xml`; raw `Int` colors cause `Resources$NotFoundException`.
- **Preferences**: DataStore for user preferences (e.g. terms acceptance, notification settings).

## What to Avoid

- Do not commit `release-key.jks`, `keystore.properties`, `.apk` files, `.db*` files, or root-level screenshots.
- Only screenshots inside `screenshots/` are intended for the README.
- Do not add Hilt unless the AGP/DSL situation is verified to support it.

## Verification Before Pushing

1. `./gradlew assembleDebug` succeeds.
2. `./gradlew assembleRelease` succeeds (or produces the expected unsigned APK if no keystore).
3. If UI/screenshots changed, confirm only intended images are in `git status`.
