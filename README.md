# Gym Tracker Android

Gym Tracker is a local-first Android workout tracker built around a WebView app shell.

## Current Baseline

- First stable baseline: `1.0`
- Android package: `com.homeops.gymtracker`
- Version code: `1`
- Version name: `1.0`
- Primary asset: `app/src/main/assets/gym_tracker_app.html`

The v1.0 baseline is cleaned for broader use:

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

The app update plan is:

1. Publish signed APKs as GitHub Release assets.
2. Publish `latest.json` with the latest version metadata.
3. The app checks `latest.json`.
4. If a newer `versionCode` exists, the app downloads and verifies the APK hash.
5. Android prompts the user to approve the update install.

Android does not allow a normal app to silently update itself.
