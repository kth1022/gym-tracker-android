# Friends And Online Sync

Knurl supports local workout partners and optional online friend sync.

## Local Friends

Local friends are stored on the device. They are used for group workouts, nearby sharing, imported group data, and temporary workout members.

## Online Friend Sync

Online Friend Sync is optional. Enable it from the Friends tab to register the device with the Knurl social API.

After sync is enabled, Knurl shows an 8-character cloud friend code. Another user can add that code by:

- scanning the cloud QR code from Show My QR
- entering the 8-character code from Enter Code

## Shared Data

Online sync sends lightweight social data only:

- display name
- cloud friend code
- workouts completed this week
- consecutive workout weeks
- last workout date
- thumbs-up and fire reactions

Full workout logs, notes, body weight, sleep, and plan details are not synced through the social API.

## Reaction History

The **You** tab shows the number of thumbs-up and fire reactions received, along with total and unread counts. The Friends tab keeps the detailed inbox entries with the sender and workout date. Marking the inbox read changes the unread count but does not delete the lifetime totals.

## Current Limitations

Reaction counts require the deployed social API summary route. If an older worker is still active, friend sync continues to work and the last locally cached counts remain visible until the worker is updated.
