# Gym Tracker Feedback Relay

Cloudflare Worker replacement for the HomeOps/Tailscale Gym Tracker feedback relay.

## Contract

The Android app posts `GymTrackerGitHubIssueSubmitV1` JSON to:

```text
/api/gym-tracker/feedback
```

The Worker validates the payload, rate-limits by client IP, filters labels, and creates a GitHub Issue in `kth1022/gym-tracker-android`.

## Secrets

Set this in Cloudflare with Wrangler:

```powershell
npx wrangler secret put GITHUB_TOKEN
```

Do not commit the token to the repository.

## Deploy

```powershell
npm install
npm run types
npm run check
npm run deploy
```

Production endpoint:

```text
https://gym-tracker-feedback-relay.kth1022.workers.dev/api/gym-tracker/feedback
```
