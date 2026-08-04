# Gym Tracker Feature Backlog

Use this file to collect feature ideas between releases. Add ideas here as they come up, then batch several related items into the next app update instead of publishing a new APK for every single feature.

## Release Batching Rule

- Add new ideas to `Candidate Features`.
- Move items to `Planned For Next Update` when they are clearly useful, scoped, and worth shipping together.
- Start implementation when the planned list has several worthwhile items or one urgent fix.
- Every shipped update still needs an increased `versionCode`, rebuilt APK, updated `latest.json`, and a GitHub Release asset.

## Planned For Next Update

No items selected yet.

## Candidate Features

Add new ideas below using this format:

```markdown
- [ ] Feature name
  - Source: user / trainer / testing / bug report
  - Notes: brief description, workflow, or expected behavior
  - Priority: low / medium / high
```

- [ ] Google Drive and Google Sheets export/import
  - Source: user suggestion
  - Notes: Allow users to export plans, workout logs, group member data, and recovery data to Google Drive in a Google Sheets-accessible format without leaving the app. Also evaluate importing plans or data directly from Google Sheets inside the app with validation, preview, duplicate detection, and conflict handling.
  - Priority: medium

- [ ] In-app feedback and feature request link
  - Source: user suggestion
  - Notes: Add Feedback and Request Feature forms that are completed inside the app rather than sending users to a browser. Route submissions to GitHub Issues or another reviewable backend queue without embedding GitHub credentials in the APK. Use structured fields for bug reports, feature requests, data recovery issues, and general feedback. Include non-sensitive app context such as app version and device details where practical.
  - Priority: medium

## Later Ideas

Use this section for useful ideas that are not ready for the next release.

## Completed

Move shipped items here with the release version.

- [x] Rest timer between sets
  - Shipped: v1.4
  - Notes: Added quick in-app rest timers and timed-exercise timer support.

- [x] Rest-day body weight and sleep tracking
  - Shipped: v1.4
  - Notes: Added rest-day body weight, sleep, and notes entry.

- [x] Show previous set values during group workouts
  - Shipped: v1.4
  - Notes: Group workout logging now shows each member's previous values for the exercise.

- [x] Time-based rep input for timed exercises
  - Shipped: v1.4
  - Notes: Timed targets use a seconds-style input and can record elapsed timer time.
