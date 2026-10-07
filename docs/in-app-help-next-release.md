# In-App Help Updates For Next Release

The in-app Help screen lives in `app/src/main/assets/gym_tracker_app.html`. Do not update it for documentation-only work. Update it during the next app release when user-facing workflows are finalized.

## Required Help Topics

- App updates
  - Explain that the app checks GitHub-hosted updates automatically and shows an in-app notice.
  - Explain that users can still manually check from the User tab.
  - Explain that Android still asks the user to approve APK installation.

- Feedback and feature requests
  - Explain how to submit bug reports, feature requests, general feedback, and data recovery requests inside the app.
  - Explain that feedback now submits through the public Cloudflare relay, so Tailscale is not required.
  - Mention that data recovery reports include diagnostics, not full workout history.

- Data recovery
  - Explain when to use Data Recovery.
  - Explain that **Restore Recovery Snapshot** restores the plan, workout logs, group data,
    rest-day entries, stretch data, and preferences together, and replaces current local data.
  - Explain when to export a full recovery snapshot for private troubleshooting.

- Set logging
  - Explain that a set stays a draft until Log set is pressed, so typing a value does not complete the workout.
  - Explain that reps are required and weight is optional, so bodyweight work logs without a weight.
  - Explain that entering zero weight is displayed as BW or Bodyweight.
  - Explain the Edit action for reopening a logged set.
  - Explain the separate Clear set data and Delete set controls.
  - Explain the Target chip showing the plan's sets and reps on each exercise card.
  - Explain that Use last is available per member in a group workout.

- Date navigation
  - Explain that Today returns the Log screen to the current date after browsing other days.

- Plan management
  - Explain importing workout plan workbooks.
  - Explain that workout-data import selects the workbook's detected date range by default.
  - Explain that importing a new plan preserves dates that already have logged workout data.
  - Explain that an imported plan starts from the import date and never rewrites past days, and starts on the next workout day when the import date already has a logged workout.
  - Explain exporting a blank workout plan template.
  - Explain exporting a filled sample workout plan workbook.
  - Explain clearing a day to rest day.
  - Explain loading a weekday plan onto an empty or rest day.

- Timed exercises
  - Explain timed rep targets, zero body-weight weight entry, and the in-app timer workflow.

- Rest-day logging
  - Explain body weight, sleep, and notes entry on rest days.

- Group workouts
  - Explain that group workouts show previous set values for the selected member and exercise.

- Online friend sync
  - Explain that users can enable Online Friend Sync from Friends.
  - Explain cloud friend codes, cloud QR codes, and entering an 8-character code.
  - Explain that only streak summaries and encouragement reactions are synced.
  - Explain that the You screen shows received reaction totals and unread counts, while Friends keeps sender and workout-date details.

- Timer controls
  - Explain that the rest timer starts with one open-ended Start Rest button and can be paused, resumed, and reset from the Log screen.
  - Explain that the rest timer flashes at each 30-second mark.
  - Explain that workout elapsed time can be paused, resumed, or reset from Session details without deleting logged data, and that only exercise activity advances it after the initial start.

- Exercise notes and editing
  - Explain that an exercise menu can save a note for the next time the exercise is performed.
  - Explain that the exercise name and description can be edited for today and optionally applied to matching weekdays going forward.

- Appearance
  - Explain that Custom theme colors are selected with color pickers and apply to this device.

## Update Rule

Every future build that changes a user workflow must update this list first, then update the in-app Help screen before the release APK is published.
