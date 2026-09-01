# Knurl Release Checklist

Use this checklist for every app build that will be shared with testers or published to users.

## Release Cycle (Required)

**Every change ships through this cycle. No exceptions, including one-line fixes.**

```
change  ->  tester build  ->  user verifies on the phone  ->  publish full version
```

1. **Change.** Make the fix and prove it with an automated check where one is possible.
2. **Tester build.** Build a `-testN` APK with its own `versionCode`, install it, and hand
   it to the user. Never publish straight from a change.
3. **Verify.** The user confirms the change on their own device with their own data.
   A passing test suite is not verification; only the user's confirmation is.
4. **Publish.** Only after that confirmation, build the release version, update
   `latest.json`, tag, and create the GitHub Release.

If a tester build comes back with a problem, the cycle restarts at step 1 with a new
`-testN` build. Do not fold an unverified fix into the publish step.

Why this is mandatory: v1.9 was built, hashed, verified against its own checksum,
published, and announced without anyone installing it. It could not install at all -
wrong signing key - so every fix in it reached nobody, and the follow-up bug reports were
really the old build still running. Local verification proves the artifact matches what
was built. Only installing it proves it works.

- [ ] The change has a tester build.
- [ ] The user has installed that tester build and confirmed the behaviour.
- [ ] The published build is the same code the user verified.

## Before Building

- [ ] Move selected items from `FEATURE_BACKLOG.md` into `Planned For Next Update`.
- [ ] Confirm each backlog item has a linked GitHub Issue.
- [ ] Confirm issue labels are correct: `bug` for defects, `feature request` plus `enhancement` for features.
- [ ] Confirm each planned item has a clear test workflow.
- [ ] Confirm no personal data ships in the APK: `PROFILES` entries have empty `clients`,
      no personal names are hardcoded in `app/src/main/assets/gym_tracker_app.html`, and
      `APPDATA` / `CUSTOM_PLAN` carry only generic sample content. Tracked by #26.
- [ ] Update the in-app Help plan in `docs/in-app-help-next-release.md`.
- [ ] Update the repo wiki source in `docs/wiki/` for any changed user workflow.
- [ ] Decide whether GitHub Wiki should be mirrored before or after tester approval.

## App Versioning

Version now comes from `gradle.properties` (`knurlVersionCode`, `knurlVersionName`) and can
be overridden per build with `-P`. `app/build.gradle` fails the build if `knurlVersionCode`
is not strictly greater than the `versionCode` in the published `latest.json`.

**Every build that leaves this machine needs its own `versionCode`, tester builds included.**
Reusing the published release's code is what caused tester build `1.8-test8 (24)` to collide
with release `1.8 (24)`: the in-app update check compares `latestVersionCode <=
BuildConfig.VERSION_CODE`, so a tester on a reused code is told "Knurl is up to date" and can
never be served a newer tester build, and feedback reports cannot be traced to a build.

### Choosing the version number

**If the only changes since the last release are bug fixes, publish an incremental
(patch) version. Reserve a new minor version for new features.**

- v1.10 ships. Bug reports come in. Fixing only those bugs gives **v1.10.1**, not v1.11.
- More bug reports against v1.10.1, fixed the same way, give **v1.10.2**.
- The next release that adds a feature or changes a workflow gives **v1.11**.

A patch release may still carry build or packaging fixes, documentation, and in-app Help
corrections. What makes it a minor release is new user-facing capability.

`versionCode` is separate and always increases by one for every build that leaves this
machine, patch and tester builds included. It never resets or mirrors the version name.

- [ ] Confirm the version name matches what actually changed: patch for fixes only,
      minor for anything new.
- [ ] Raise `knurlVersionCode` past the last published release, even for a tester build.
- [ ] Raise `knurlVersionName` when the build is intended for a new public release.
- [ ] Give tester builds a distinguishable `versionName` suffix, for example `1.9-test1`.

```powershell
# Tester build
./gradlew assembleDebug -PknurlVersionCode=26 -PknurlVersionName=1.9-test1

# Release build (uses the gradle.properties baseline)
./gradlew assembleDebug
```

- [ ] Verify the app displays the intended version in the User tab.
- [ ] Verify a tester build reports its own version in a submitted feedback issue.

## App Signing

Android refuses to install an APK over an app signed with a different key. The user sees
`App not installed` and their only recourse is to uninstall, which destroys their logged
workouts. **A signing-key change is a data-loss event, not an inconvenience.**

Default Android debug keystores expire after one year and the tooling silently generates a
replacement. That is exactly what happened between v1.8 and v1.9: the keystore was
regenerated on 2026-08-25, so release v1.9 (`dd84967b...`) could not install over v1.8
(`030f60f9...`) on any existing device. The release looked successful and was unusable.

`app/build.gradle` now pins the keystore and fails the build when its SHA-256 fingerprint
does not match `knurlSigningSha256` in `gradle.properties`.

- Canonical keystore: `~/.android/debug.keystore`
- Backup: `~/.android/knurl-signing-key-backup.keystore`
- Override for a one-off build: `-PknurlKeystore=/path/to/debug.keystore`

The keystore is never committed. `.gitignore` covers `*.keystore` and `*.jks`, and the
GitHub repo is public, so publishing it would let anyone sign a package that installs over
Knurl on a user's phone.

- [ ] Confirm the build's signing fingerprint matches the previous release:
      `apksigner verify --print-certs <apk>`.
- [ ] Back up the keystore before any Android SDK or machine change.
- [ ] Never change `knurlSigningSha256` unless every user is going to uninstall and reinstall.

## Build And Test

- [ ] Build the APK.
- [ ] Install the APK on a test phone.
- [ ] Test the changed workflows from the backlog.
- [ ] Test update checking from the User tab.
- [ ] Test any import/export workflow touched by the release.
- [ ] Test feedback and data recovery submission if those areas changed.

## Documentation

- [ ] Update the in-app Help screen before the release build when user-facing workflows changed.
- [ ] Update `README.md` when version, install, update, or support behavior changed.
- [ ] Update `FEATURE_BACKLOG.md` so shipped items move to `Completed`.
- [ ] Update `docs/wiki/` with release notes, user guidance, and troubleshooting changes.
- [ ] Mirror `docs/wiki/` to the GitHub Wiki when the release is approved.

## Publish

- [ ] Confirm the user verified a tester build of exactly this code (see Release Cycle).
- [ ] Build the final approved APK.
- [ ] Generate APK size and SHA-256.
- [ ] Update `latest.json` with version, download URL, APK size, and hash.
- [ ] Commit the release changes.
- [ ] Push the commit and tag.
- [ ] Create or update the GitHub Release with the APK and `latest.json`.
- [ ] Verify the public `latest.json` URL serves the new metadata.
- [ ] Verify the public APK URL downloads the expected file.
- [ ] Verify the published APK's signing fingerprint matches the previous release before
      announcing it, by downloading the published asset rather than trusting the local build.
