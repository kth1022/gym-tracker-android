# Knurl Maintainer Handoff

## Current State

- Current public release: Knurl `1.9.1`, version code `29`.
- Source batch commit: `7f137dc Implement small backlog feature batch`.
- Source batch was pushed to `main`; no public release was published.
- Tester batch: `1.9.2-test1`, version code `30`.
- The source batch includes backlog issues #33, #39, #15, and #35/#36.
- In-app Help, wiki source, and `FEATURE_BACKLOG.md` were updated.

## Signing-Key Recovery

The original Knurl signing keystore was not found after the 2026-09-16 computer rebuild. It is required before producing an installable tester update.

Expected path:

`C:\Users\kth10\.android\debug.keystore`

Documented backup path:

`C:\Users\kth10\.android\knurl-signing-key-backup.keystore`

Required SHA-256 fingerprint:

`03:0F:60:F9:11:D2:D0:2B:4B:0E:19:AC:DF:66:DC:F5:CA:8A:D4:AE:55:51:5A:24:8A:ED:01:AB:DD:1F:89:28`

Searches of the current computer, all of `D:\`, `D:\rebuild-20260916`, the working tree, and Git history found no matching Knurl keystore. Existing APKs do not contain the private signing key. The discovered `homeops-remote-debug.keystore` files belong to another application and must not be used.

## Release Safety

Do not generate a replacement key, publish `latest.json`, create a GitHub Release, or ask users to uninstall Knurl. Restore and verify the original key first. After restoration, build the tester APK with version code `30`, install it over Knurl `1.9.1`, and wait for phone verification before publishing.

The full working note is maintained in the Second Brain vault at:

`C:\Users\kth10\Second Brain\10 Home\Handoffs\knurl-gym-tracker.md`
