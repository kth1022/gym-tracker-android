# Import, Export, And Templates

Gym Tracker uses workbook and JSON exports for plans, logs, recovery snapshots, and troubleshooting.

## Workout Plan Workbooks

Workout plan imports must use the Gym Tracker plan workbook structure. The safest starting point is the blank workout plan template exported by the app.

## Recovery Snapshot

A recovery snapshot is a private JSON export that can help restore or inspect app data after an update, phone change, or import problem.

Use a recovery snapshot when:

- A user's plan disappears after updating.
- Group workout member data is missing.
- Older app data needs to be inspected.
- The maintainer asks for full recovery data privately.

## Data Recovery Issue

A Data Recovery issue is different from a full recovery snapshot. It sends useful diagnostics and storage inventory metadata to GitHub Issues through the feedback relay. It does not automatically post full workout history.

## Future Google Drive And Sheets Support

Google Drive and Google Sheets import/export remains a backlog item. The intended workflow is fully in-app, without requiring users to leave Gym Tracker.

