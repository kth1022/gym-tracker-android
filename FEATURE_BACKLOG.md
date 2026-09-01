# Knurl Feature Backlog

Use this file to collect feature ideas between releases. Add ideas here as they come up, then batch several related items into the next app update instead of publishing a new APK for every single feature.

## Release Batching Rule

- Add new ideas to `Candidate Features`.
- Create or link a GitHub Issue for every backlog item so users can see known bugs and requested features.
- Categorize GitHub Issues correctly: use `bug` for defects and `feature request` plus `enhancement` for feature ideas.
- Move items to `Planned For Next Update` when they are clearly useful, scoped, and worth shipping together.
- Start implementation when the planned list has several worthwhile items or one urgent fix.
- Every tester build and public release must follow `docs/release-checklist.md`.
- Every shipped update still needs an increased `versionCode`, rebuilt APK, updated `latest.json`, a GitHub Release asset, updated in-app Help when workflows change, and updated wiki source in `docs/wiki/`.

## Planned For Next Update

Nothing selected yet. v1.9 shipped on 2026-08-31; see `Completed`.

Baseline version for the next build is in `gradle.properties`. Raise `knurlVersionCode`
for every build that leaves this machine, tester builds included — the build now fails
if it is not above the published release. See `docs/release-checklist.md`.

## Candidate Features

Add new ideas below using this format:

```markdown
- [ ] Feature name
  - GitHub Issue: #123
  - Category: feature / bug
  - Source: user / trainer / testing / bug report
  - Notes: brief description, workflow, or expected behavior
  - Priority: low / medium / high
```

- [ ] Google Drive and Google Sheets export/import
  - GitHub Issue: #3
  - Category: feature
  - Source: user suggestion
  - Notes: Allow users to export plans, workout logs, group member data, and recovery data to Google Drive in a Google Sheets-accessible format without leaving the app. Also evaluate importing plans or data directly from Google Sheets inside the app with validation, preview, duplicate detection, and conflict handling.
  - Priority: medium

- [ ] Superset set entry with two weights and two reps
  - GitHub Issue: #9 (absorbed #24, closed as duplicate)
  - Category: feature
  - Source: user feedback
  - Notes: When logging a superset, a set entry must capture two weights *and* two reps, one pair per movement in the pair. Normal single-exercise set entry must be unchanged. Volume, Use Last, rep deltas, Progress/Trend, and export/import must all account for both halves of the pair.
  - Priority: medium

- [ ] Split set-management controls
  - GitHub Issue: #15
  - Category: feature
  - Source: user feedback
  - Notes: Restore separate, quick controls for clearing a set and deleting a set instead of requiring a menu for both actions.
  - Priority: medium

- [ ] Exercise-specific notes
  - GitHub Issue: #17
  - Category: feature
  - Source: user clarification
  - Notes: Allow a short note on an individual exercise, separate from the existing workout-level notes. It should be visible when that exercise is next performed; decide whether it follows the exercise forward when the plan is updated.
  - Priority: medium

- [ ] Edit exercise name and description, then update the plan
  - GitHub Issue: #18
  - Category: feature
  - Source: user feedback
  - Notes: Let a user rename an exercise and edit its description from the log, then apply that change to the matching weekday going forward. The current swap and target-edit tools do not support arbitrary names or descriptions.
  - Priority: medium

- [ ] Show received friend reaction counts on the You screen
  - GitHub Issue: #21
  - Category: feature
  - Source: user testing
  - Notes: On the You screen, show how many of each reaction type (for example, thumbs-up and fire) the user has received from online friends. Keep the Friends inbox for sender and workout-date detail.
  - Priority: medium

## Later Ideas

Use this section for useful ideas that are not ready for the next release.

## Completed

Move shipped items here with the release version.

- [x] Restore Use Last in group workout logging
  - GitHub Issue: #14
  - Shipped: v1.9
  - Category: bug
  - Priority: high
  - Change: the group member card renders a `Use last` button again, and the `uselast`
    handler resolves the correct person and profile via `data-person` instead of always
    writing to the current client's session.
  - Test workflow: run a group workout with two members who each have prior data for the
    same exercise. Confirm both members show `Use last`, that tapping it fills only that
    member's sets, that the copied values come from that member's own history, and that
    the standard single-user workout still shows and honours `Use last`.

- [x] Reset online reaction buttons after send
  - GitHub Issue: #20
  - Shipped: v1.9
  - Category: bug
  - Priority: high
  - Change: a successful `sendSocialReaction` clears the stored selection for that friend
    and workout, so the button returns to its unselected state. A failed send still keeps
    the reaction locally so nothing is lost offline.
  - Test workflow: send a thumbs-up to an online friend and confirm the button clears and
    another reaction can be sent. Then turn off networking, send a reaction, and confirm it
    stays selected with the "saved locally" message.

- [x] Show planned sets and reps in the standard workout log
  - GitHub Issue: #22
  - Shipped: v1.9
  - Category: bug
  - Priority: high
  - Change: standard exercise cards show a `Target: 3 sets × 10-12` chip alongside the
    existing `Set n of m` chip, matching what group cards already displayed. The chip is
    omitted when the plan specifies no prescription.
  - Test workflow: open a plan day with a full prescription and confirm the target chip
    matches the plan. Confirm an exercise with only sets, only reps, or neither renders
    sensibly rather than showing an empty target.

- [x] Keep set entry as a draft until Log Set is pressed, and allow edits afterward
  - GitHub Issue: #23
  - Shipped: v1.9
  - Category: bug
  - Priority: high
  - Change: sets carry a `logged` flag. Typing a weight or reps, or using the +/- steppers,
    marks the set a draft and no longer flips the workout to Complete. `Log set` commits it;
    a new `Edit` action on a logged row reopens it as a draft. Status, volume, set counts,
    exports and "last time" lookups all count only committed sets. Sets saved before this
    flag existed are still treated as logged.
  - Test workflow: type a weight on a scheduled workout and confirm the day stays
    Scheduled and the calendar does not mark it complete. Press `Log set` and confirm it
    becomes Complete. Press `Edit` on a logged set, confirm it reopens for editing, and
    re-log it. Confirm a bodyweight exercise logs with reps and no weight. Confirm a
    workout logged in a previous version still reads as Complete after upgrading.

- [x] Plan import must not be retroactive
  - GitHub Issue: #25
  - Shipped: v1.9
  - Category: bug
  - Priority: high
  - Change: plan import now clips the incoming plan to an effective start date before
    merging. The plan starts on the upload date, or on the next day the incoming plan
    schedules a workout when the upload date already has logged data. Past dates keep
    their existing template, `dateMin` is no longer pulled backwards by an import, and
    the toast reports the start date and how many past days were left alone. Importing a
    plan that is entirely in the past is refused with a clear message.
  - Test workflow: import a plan whose workbook starts weeks in the past on a day with no
    logged workout, and confirm it starts today with past days untouched. Log a workout
    today, import again, and confirm it starts on the next scheduled day and today's log
    survives. Import a plan that is entirely in the past and confirm nothing is imported.
    Confirm the v1.7 behaviour for #5 still holds on previously logged days.

- [x] Ship only generic plan content in the APK
  - GitHub Issue: #26
  - Shipped: v1.9
  - Category: bug
  - Priority: high
  - Change: the built-in legacy v24 plan shipped one real person's working weights in
    `kw`/`kr`/`bw`/`br` on 32 exercises; those fields are stripped and the generic
    exercise, sets, reps and coaching notes remain. Legacy detection no longer matches
    hardcoded names - it keys off the legacy storage keys and recovers client names from
    the user's own data. `app/build.gradle` now fails the build if the shipped asset
    contains a non-empty `clients` roster, a denylisted personal name, or numeric
    `kw/kr/bw/br` values.
  - Test workflow: confirm a clean build passes and that dirtying the asset with a
    personal name, a client roster, or a numeric working weight fails the build with a
    clear message. Install on a device with no prior data and confirm onboarding shows
    only "Main Plan" with no clients. Confirm legacy recovery still works from legacy
    storage keys.

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

- [x] Automatic update availability notification
  - GitHub Issue: #4
  - Shipped: v1.7
  - Notes: The app checks for updates automatically at startup and periodically while open, shows an in-app update banner when a newer GitHub-hosted version is available, and keeps the manual User tab check.

- [x] Preserve completed week data when importing a new plan
  - GitHub Issue: #5
  - Shipped: v1.7
  - Notes: Plan imports preserve dates that already have logged workout data so completed days remain connected to their original workout template.

- [x] Provide a sample Excel workout plan file
  - GitHub Issue: #6
  - Shipped: v1.7
  - Notes: The Plan tab exports a filled sample `GymTrackerPlanV1` workbook showing valid plan sheets, headers, dates, exercises, timed targets, and stretching rows.

- [x] Online friend streak sharing and encouragement reactions
  - GitHub Issue: #19
  - Shipped: v1.8
  - Notes: Friends can opt in to online sync, share lightweight workout streak summaries, add online friends by cloud code or QR code, and send thumbs-up or fire encouragement reactions through the Cloudflare Worker and D1 social API.
