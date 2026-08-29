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

## Current Limitations

Reactions are sent through the Cloudflare Worker, but the received reaction experience still needs refinement. The active backlog tracks clearer in-app reaction visibility and resetting reaction buttons after a send.
