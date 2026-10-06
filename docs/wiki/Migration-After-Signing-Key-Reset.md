# Moving To A Newly Signed Knurl App

Use these steps only when Knurl has been rebuilt with a replacement signing key and Android will not install it over the existing app. Do not uninstall the old app until the export files are saved somewhere you can access after reinstalling.

## Before Uninstalling The Old App

1. Open the current Knurl app and confirm that your plan, workout history, and group members are present.
2. Open the **Plan** tab and open **Export**.
3. Tap **Export Workout Data** and save the workbook somewhere safe, such as Downloads or a private cloud folder. This is the file used to restore workout logs.
4. Tap **Export Plan** and save the plan workbook somewhere safe. This is the file used to restore the plan.
5. Tap **Save Recovery Snapshot** and save the JSON file privately. This is now the preferred all-in-one restore file because it includes the plan, workout logs, group data, rest-day entries, and preferences.
6. Open each saved file or check the file manager and confirm that all three files exist before continuing.

## Install The New App

1. Download the replacement-signed Knurl tester APK supplied by the maintainer.
2. If Android asks, allow the browser or file manager to install unknown apps.
3. Open Android **Settings > Apps > Knurl** and uninstall the old Knurl app. This is required because the replacement APK has a different signing key.
4. Open the downloaded APK and install it.
5. Open Knurl and complete the initial setup using the same account name.

## Restore The Snapshot

1. Open the **Plan** tab and open **Import**.
2. Tap **Restore Recovery Snapshot** and choose the saved JSON file.
3. Confirm the replacement warning. The snapshot replaces the current local data and restores the plan, workout logs, group data, rest-day entries, and preferences together.
4. Review several current and older dates, including a group workout, a rest day, and a day with body weight or sleep data.

Use the separate workbook steps below only when the snapshot is unavailable or when you intentionally want to restore only part of the data.

## Restore The Plan And Logs From Workbooks

1. Open the **Plan** tab and open **Import**.
2. Tap **Import Plan** and choose the saved plan workbook.
3. Open **Plan > Import** again and tap **Import Workout Data**.
4. Choose the saved workout-data workbook.
5. The app detects the workbook's date range and selects the full range by default. Confirm that range before importing.
6. Review several current and older dates, including a group workout, a rest day, and a day with body weight or sleep data.

## If Something Is Missing

Stop before entering new data. Keep the saved JSON recovery snapshot and the two workbooks. Submit an in-app **Data Recovery** request and share the recovery files privately with the maintainer if requested. Never attach the recovery snapshot to a public GitHub issue unless the maintainer explicitly provides a private transfer method.

This walkthrough is for the replacement-key migration only. A normal signed update should be installed over the existing app and does not require uninstalling or restoring data.
