# Knurl Maintainer Handoff

## Current State

- Current public release: Knurl `1.9.1`, version code `29`.
- Source batch commit: `7f137dc Implement small backlog feature batch`.
- Source batch was pushed to `main`; no public release was published.
- Tester batch: `1.9.2-recovery-test3`, version code `32`.
- The source batch includes backlog issues #33, #39, #15, and #35/#36.
- In-app Help, wiki source, and `FEATURE_BACKLOG.md` were updated.
- The replacement tester APK was built locally and has not been published.

## Signing-Key Recovery

The original Knurl signing keystore was not found after the 2026-09-16 computer rebuild. It is considered lost. A replacement key was generated for the migration build, so the replacement APK cannot update existing installations; users must export their data, uninstall the old app, install the replacement, and import their data.

Expected path:

`C:\Users\kth10\.android\debug.keystore`

Documented backup path:

`C:\Users\kth10\.android\knurl-signing-key-backup.keystore`

Replacement key SHA-256 fingerprint:

`9F:80:ED:DC:12:45:7E:AF:E4:71:11:3F:CB:E1:A1:E7:DD:66:25:02:6B:B5:01:18:73:62:21:FB:48:DD:B2:44`

Searches of the current computer, all of `D:\`, `D:\rebuild-20260916`, the working tree, and Git history found no matching Knurl keystore. Existing APKs do not contain the private signing key. The discovered `homeops-remote-debug.keystore` files belong to another application and must not be used.

The replacement key is currently at the canonical Android debug path and has a second local backup:

- Canonical: `C:\Users\kth10\.android\debug.keystore`
- Local backup: `C:\Users\kth10\.android\knurl-signing-key-backup.keystore`

The keystore and its password are not committed. The TrueNAS encrypted backup and Vaultwarden password entry still need to be created before any public release.

## Release Safety

Do not publish `latest.json` or create a GitHub Release until the replacement migration APK is tested. Do not tell users to install it as an update. The required migration is: export Workout Data, export the Plan, save a Recovery Snapshot, uninstall the old Knurl installation, install the replacement APK, import the Plan and Workout Data, and verify the recovered history and group data.

If the key is not found anywhere on `D:\`, it is considered lost in the 2026-09-16 computer rebuild. A replacement key cannot update existing installations; users would need a data export/recovery workflow before uninstalling and reinstalling a newly signed app.

Future signing-key backups will use a restricted TrueNAS location for the encrypted keystore file and Vaultwarden for the keystore password. Neither the keystore nor its password may be committed to GitHub.

## Build Environment

- JDK: `C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot`
- Android SDK: `C:\Program Files (x86)\Android\android-sdk`
- Installed compile/build tools: Android API 36 and Build Tools `36.0.0`
- The project pins `buildToolsVersion "36.0.0"` because Build Tools 34 is not installed on this machine.

Tester build command:

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot'
./gradlew.bat assembleDebug '-PknurlVersionCode=32' '-PknurlVersionName=1.9.2-recovery-test3'
```

The full working note is maintained in the Second Brain vault at:

`C:\Users\kth10\Second Brain\10 Home\Handoffs\knurl-gym-tracker.md`
