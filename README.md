# Gym Tracker Android

Gym Tracker is a local-first Android workout tracker built around a WebView app shell.

## Current Baseline

- Current update baseline: `1.7`
- Android package: `com.homeops.gymtracker`
- Version code: `16`
- Version name: `1.7`
- Primary asset: `app/src/main/assets/gym_tracker_app.html`

The v1.0 baseline was cleaned for broader use:

- no packaged personal workout history
- no hardcoded personal user names
- clean first-run onboarding
- generic preloaded workout plans
- user-facing export headers use `User`
- older imports using `Client` are still accepted for backward compatibility

## Build Locally

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME="$env:LOCALAPPDATA\Android\Sdk"
$env:ANDROID_SDK_ROOT="$env:LOCALAPPDATA\Android\Sdk"
.\gradlew.bat assembleDebug
```

Debug APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Release Signing

Do not publish debug APKs as the long-term update channel.

Before GitHub-hosted app updates are enabled, create a release keystore and configure `keystore.properties` locally or through GitHub Actions secrets. Android requires future APK updates to use the same package name and signing key.

Required release invariants:

- same `applicationId`
- same release signing key
- increasing `versionCode`

## GitHub Updates

The app checks for updates quietly on startup and supports manual checks from the User tab:

1. Publish APKs as GitHub Release assets.
2. Publish `latest.json` with the latest version metadata, APK URL, size, and SHA-256 hash.
3. The app checks `latest.json` quietly on startup, periodically while open, and when the user taps `Check for Update`.
4. If a newer `versionCode` exists, the app shows an update banner and the User tab shows the available update.
5. When the user taps `Download Update`, the app downloads and verifies the APK hash.
6. Android prompts the user to approve the update install.

Android does not allow a normal app to silently update itself.

## Documentation

Release documentation uses two layers:

- In-app Help: short workflow guidance inside `app/src/main/assets/gym_tracker_app.html`.
- Wiki source: longer guides in `docs/wiki/`, intended to be mirrored to the GitHub Wiki.

Use `docs/release-checklist.md` for every tester build and public release. Future releases that change user workflows must update both the in-app Help plan and the wiki source before publishing.

## Feedback Relay

In-app feedback and data recovery requests submit to GitHub Issues through the Cloudflare Worker in `cloudflare/gym-tracker-feedback-relay`.

Production endpoint:

```text
https://gym-tracker-feedback-relay.kth1022.workers.dev/api/gym-tracker/feedback
```

The Worker stores the GitHub token as a Cloudflare secret. Do not embed GitHub tokens in the APK.

## Legacy v24 Migration

Version `1.2` includes a repair migration for users updating from `gym_tracker_v24_debug.apk`.

The migration preserves old `log-kb-all`, `plan-kb`, and `stretch-kb-all` data, reconstructs missing v24 built-in workout days when no saved plan exists, keeps group-member logs under friend profiles, and adds export tools for all workout data plus a raw recovery snapshot.

Version `1.3` tightens the repair so completed logged workouts replace generic legacy templates on affected dates. This helps recover beginning-of-week sessions from older revisions after v1.2 has already run.

## Version 1.6

Version `1.6` adds the batched backlog update: in-app feedback to GitHub Issues through the HomeOps relay, data-recovery diagnostics, timed set and rest timer refinements, rest-day body weight and sleep entry, group-workout previous-set references, workout day rescheduling, and blank workout plan template export/import support.

## Version 1.7

Version `1.7` adds automatic update notices, preserves logged workout days when importing a new plan, and adds a filled sample workout plan workbook export.
