# In-App Help Updates For Next Release

The in-app Help screen lives in `app/src/main/assets/gym_tracker_app.html`. Do not update it for documentation-only work. Update it during the next app release when user-facing workflows are finalized.

## Required Help Topics

- App updates
  - Explain that the app can check GitHub-hosted updates from the User tab.
  - Explain that Android still asks the user to approve APK installation.

- Feedback and feature requests
  - Explain how to submit bug reports, feature requests, general feedback, and data recovery requests inside the app.
  - Mention that data recovery reports include diagnostics, not full workout history.

- Data recovery
  - Explain when to use Data Recovery.
  - Explain when to export a full recovery snapshot for private troubleshooting.

- Plan management
  - Explain importing workout plan workbooks.
  - Explain exporting a blank workout plan template.
  - Explain clearing a day to rest day.
  - Explain loading a weekday plan onto an empty or rest day.

- Timed exercises
  - Explain timed rep targets, zero body-weight weight entry, and the in-app timer workflow.

- Rest-day logging
  - Explain body weight, sleep, and notes entry on rest days.

- Group workouts
  - Explain that group workouts show previous set values for the selected member and exercise.

## Update Rule

Every future build that changes a user workflow must update this list first, then update the in-app Help screen before the release APK is published.

