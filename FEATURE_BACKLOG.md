# Gym Tracker Feature Backlog

Use this file to collect feature ideas between releases. Add ideas here as they come up, then batch several related items into the next app update instead of publishing a new APK for every single feature.

## Release Batching Rule

- Add new ideas to `Candidate Features`.
- Move items to `Planned For Next Update` when they are clearly useful, scoped, and worth shipping together.
- Start implementation when the planned list has several worthwhile items or one urgent fix.
- Every tester build and public release must follow `docs/release-checklist.md`.
- Every shipped update still needs an increased `versionCode`, rebuilt APK, updated `latest.json`, a GitHub Release asset, updated in-app Help when workflows change, and updated wiki source in `docs/wiki/`.

## Planned For Next Update

No additional items selected yet.

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

- [x] In-app feedback and feature request link
  - Shipped: v1.6
  - Notes: Feedback, bug reports, data recovery requests, and general feedback are completed inside the app and submitted to GitHub Issues through a secure HomeOps relay without embedding a GitHub token in the APK.

- [x] Data recovery report diagnostics
  - Shipped: v1.6
  - Notes: Data Recovery issues include useful diagnostic context and storage inventory metadata. Full workout history is not posted automatically to public GitHub Issues.

- [x] Reschedule a planned workout to another day
  - Shipped: v1.6
  - Notes: Users can clear the selected date to a rest day while preserving the replaced workout template for later use. Users can load an existing planned weekday workout onto the selected date.

- [x] Export blank workout plan template for import
  - Shipped: v1.6
  - Notes: Users can export a blank `GymTrackerPlanV1` workbook template with the same sheet names and headers accepted by the plan importer.
