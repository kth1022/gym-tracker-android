# Feedback And Data Recovery

Gym Tracker can submit feedback from inside the app. Feedback is sent through the HomeOps relay and creates GitHub Issues in `kth1022/gym-tracker-android`.

## Feedback Types

- Bug report
- Feature request
- Data recovery request
- General feedback

## Feedback Endpoint

The current feedback endpoint uses a Cloudflare Worker:

```text
https://gym-tracker-feedback-relay.kth1022.workers.dev/api/gym-tracker/feedback
```

The phone does not need to be on Tailscale for feedback submission. The Worker owns the GitHub credential as a Cloudflare secret and creates GitHub Issues without embedding a token in the APK.

## Data Recovery Requests

Use Data Recovery when a user reports missing plans, missing workout logs, missing group workout data, or a failed migration after updating.

Data Recovery issues include diagnostics and storage inventory metadata that help identify what is available on the device. Full workout history is not posted automatically to public GitHub Issues.

If full data is needed, export a recovery snapshot and share it privately.

## Privacy Notes

Do not embed GitHub tokens in the Android app. The app should submit feedback to a relay service, and the relay service should own the GitHub credential.
