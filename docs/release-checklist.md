# Knurl Release Checklist

Use this checklist for every app build that will be shared with testers or published to users.

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

- [ ] Build the final approved APK.
- [ ] Generate APK size and SHA-256.
- [ ] Update `latest.json` with version, download URL, APK size, and hash.
- [ ] Commit the release changes.
- [ ] Push the commit and tag.
- [ ] Create or update the GitHub Release with the APK and `latest.json`.
- [ ] Verify the public `latest.json` URL serves the new metadata.
- [ ] Verify the public APK URL downloads the expected file.
