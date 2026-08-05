# Gym Tracker Release Checklist

Use this checklist for every app build that will be shared with testers or published to users.

## Before Building

- [ ] Move selected items from `FEATURE_BACKLOG.md` into `Planned For Next Update`.
- [ ] Confirm each planned item has a clear test workflow.
- [ ] Update the in-app Help plan in `docs/in-app-help-next-release.md`.
- [ ] Update the repo wiki source in `docs/wiki/` for any changed user workflow.
- [ ] Decide whether GitHub Wiki should be mirrored before or after tester approval.

## App Versioning

- [ ] Increase `versionCode`.
- [ ] Increase `versionName` when the build is intended for a new public release.
- [ ] Verify the app displays the intended version in the User tab.

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

