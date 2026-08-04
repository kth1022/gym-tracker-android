# Gym Tracker Feature Backlog

Use this file to collect feature ideas between releases. Add ideas here as they come up, then batch several related items into the next app update instead of publishing a new APK for every single feature.

## Release Batching Rule

- Add new ideas to `Candidate Features`.
- Move items to `Planned For Next Update` when they are clearly useful, scoped, and worth shipping together.
- Start implementation when the planned list has several worthwhile items or one urgent fix.
- Every shipped update still needs an increased `versionCode`, rebuilt APK, updated `latest.json`, and a GitHub Release asset.

## Planned For Next Update

- [ ] Rest timer between sets
  - Source: user suggestion
  - Notes: Add a timer button on each workout screen with configurable rest duration. Include a timer mode for time-based exercises, such as planks, so users can time the exercise inside the app instead of leaving to use another timer.
  - Priority: medium

- [ ] Rest-day body weight and sleep tracking
  - Source: user suggestion
  - Notes: Allow users to enter body weight and sleep data on rest days so recovery metrics can be captured even when no workout is scheduled.
  - Priority: medium

- [ ] Show previous set values during group workouts
  - Source: user suggestion
  - Notes: When logging a group workout, show the user's previous weight and reps for that exercise just like the normal workout flow does.
  - Priority: medium

- [ ] Time-based rep input for timed exercises
  - Source: user suggestion
  - Notes: When an exercise target uses time instead of reps, such as planks with 3 sets of 30-45 sec, show the reps field as a time input. For bodyweight timed exercises, users can enter 0 for weight and enter the held time in the time/reps field. Pair this with the in-app timer so the completed time can be recorded without switching apps.
  - Priority: medium

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
