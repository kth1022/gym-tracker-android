# Updates And Releases

Knurl checks for app updates automatically and from the You tab. The app reads `latest.json`, compares version codes, downloads the APK, verifies the SHA-256 hash, and then lets Android ask the user to approve installation.

## User Update Flow

1. When Knurl sees a newer GitHub-hosted release, it shows an update notice inside the app.
2. Tap Download Update from the notice or open the You tab and tap Check for Update.
3. If a newer version is available, tap Download Update.
4. Approve the Android install prompt.
5. Reopen Knurl after installation.

Android does not allow a normal app to silently update itself.

## Maintainer Release Flow

Use `docs/release-checklist.md` before every tester build or public release.

Required release artifacts:

- APK
- `latest.json`
- GitHub Release
- Release tag
- Updated Help and Wiki documentation when workflows changed

## Build Versioning

Every build that leaves the maintainer's machine needs its own `versionCode`, tester builds
included. The version comes from `gradle.properties` and can be overridden per build with
`-PknurlVersionCode` and `-PknurlVersionName`. The build fails if the `versionCode` is not
above the published release, because a reused code makes the in-app update check report
"up to date" on a tester build and makes feedback reports impossible to trace to a build.

## Current Public Release

Version `1.9` is a bug-fix release. Imported plans are no longer retroactive, set entry stays
a draft until Log set is pressed and can be edited afterwards, exercise cards show the planned
sets and reps, Use last is back in group workouts, reaction buttons reset after sending, and
the APK no longer contains any personal plan data.

## Previous Releases

Version `1.8` introduces the Knurl redesign, online friend streak sharing, cloud friend codes and QR codes, encouragement reactions, a Cloudflare/D1 social API, one-month Progress calendar rendering, and rest timer safe-area refinements.

Version `1.7` adds automatic update notices, preserves already logged workout days during plan imports, and adds a filled sample workout plan workbook export.
