# Updates And Releases

Gym Tracker checks for app updates automatically and from the User tab. The app reads `latest.json`, compares version codes, downloads the APK, verifies the SHA-256 hash, and then lets Android ask the user to approve installation.

## User Update Flow

1. When Gym Tracker sees a newer GitHub-hosted release, it shows an update notice inside the app.
2. Tap Download Update from the notice or open the User tab and tap Check for Update.
3. If a newer version is available, tap Download Update.
4. Approve the Android install prompt.
5. Reopen Gym Tracker after installation.

Android does not allow a normal app to silently update itself.

## Maintainer Release Flow

Use `docs/release-checklist.md` before every tester build or public release.

Required release artifacts:

- APK
- `latest.json`
- GitHub Release
- Release tag
- Updated Help and Wiki documentation when workflows changed

## Current Public Release

Version `1.7` adds automatic update notices, preserves already logged workout days during plan imports, and adds a filled sample workout plan workbook export.

## Previous Release

Version `1.6` adds in-app feedback, data recovery diagnostics, rest-day body weight and sleep logging, group-workout previous-set references, workout day rescheduling, blank workout plan template export/import support, and timed exercise refinements.
