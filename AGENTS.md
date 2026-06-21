# Repository Guidelines

## Project Structure & Module Organization

This is a single-module Android application built with Kotlin and Jetpack Compose. Application code lives in `app/src/main/java/com/example/project/`; keep reusable UI theme code under `ui/theme` and group new features into focused packages. Android resources belong in `app/src/main/res`, while the manifest is `app/src/main/AndroidManifest.xml`. Put JVM unit tests in `app/src/test` and device or emulator tests in `app/src/androidTest`. Centralize dependency versions and aliases in `gradle/libs.versions.toml`.

## Build, Test, and Development Commands

Run commands from the repository root. On Windows, use the checked-in wrapper:

- `.\gradlew.bat assembleDebug` builds a debug APK.
- `.\gradlew.bat testDebugUnitTest` runs local JVM tests.
- `.\gradlew.bat connectedDebugAndroidTest` runs instrumented tests on a connected device or emulator.
- `.\gradlew.bat lintDebug` performs Android lint checks.
- `.\gradlew.bat clean` removes generated build outputs when troubleshooting stale artifacts.

Open the repository root in Android Studio to run the `app` configuration interactively. Do not commit `local.properties`, `.gradle/`, `.idea/`, or generated `build/` directories.

## Coding Style & Naming Conventions

Use Kotlin's standard four-space indentation and Android Studio's default Kotlin formatter. Keep imports explicit and remove unused imports. Name classes and composables in `PascalCase`, functions and properties in `camelCase`, and resource files/IDs in `snake_case`. Composable functions should describe visible UI, such as `ProfileScreen` or `SettingsRow`, and previews should end in `Preview`. Prefer small, stateless composables with state lifted to their caller.

## Testing Guidelines

JUnit 4 is configured for local tests; AndroidX JUnit, Espresso, and Compose UI testing are available for instrumentation tests. Name test classes after the subject under test (for example, `GreetingTest`) and use descriptive test methods such as `greeting_displaysUserName`. Add local tests for logic and instrumented tests for Android APIs, navigation, and user-visible Compose behavior. Run both unit tests and lint before submitting changes; run connected tests when UI behavior changes.

## Commit & Pull Request Guidelines

The current history uses a Conventional Commit-style subject (`chore: ...`). Continue with short imperative subjects such as `feat: add settings screen` or `fix: preserve theme selection`; a Korean or English description is acceptable if used consistently within the change. Pull requests should explain the problem and solution, list verification commands, link relevant issues, and include screenshots or recordings for UI changes. Keep each PR focused and call out manifest, permission, SDK, or dependency changes explicitly.
