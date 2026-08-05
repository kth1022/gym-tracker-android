# Feedback And Data Recovery

Gym Tracker can submit feedback from inside the app. Feedback is sent through the HomeOps relay and creates GitHub Issues in `kth1022/gym-tracker-android`.

## Feedback Types

- Bug report
- Feature request
- Data recovery request
- General feedback

## Current Endpoint Requirement

The current feedback endpoint uses the HomeOps relay at `kevin-pc.taile05f72.ts.net`. Until a public endpoint is added, the phone must be able to reach that Tailscale address.

For family testers, add the phone to Tailscale. For broader public use, replace the endpoint with a public relay such as a Cloudflare Worker or another hosted API.

## Data Recovery Requests

Use Data Recovery when a user reports missing plans, missing workout logs, missing group workout data, or a failed migration after updating.

Data Recovery issues include diagnostics and storage inventory metadata that help identify what is available on the device. Full workout history is not posted automatically to public GitHub Issues.

If full data is needed, export a recovery snapshot and share it privately.

## Privacy Notes

Do not embed GitHub tokens in the Android app. The app should submit feedback to a relay service, and the relay service should own the GitHub credential.

