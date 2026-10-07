# Knurl Accounts And Cloud Recovery

Status: active planning. This document defines the recommended first implementation
boundary for backlog issues #42-#49. It is not a deployment guide and does not authorize
production account creation yet.

## Recommended MVP

Knurl remains usable without an account. An account adds optional recovery and notification
services:

1. The user creates an account or continues locally.
2. The user explicitly enables cloud recovery.
3. Knurl creates a versioned recovery snapshot and uploads an encrypted copy.
4. A fresh install can sign in, show the latest snapshot date and size, and ask for restore
   confirmation.
5. The restore replaces local data only after confirmation. The app keeps a local safety
   snapshot before replacing data.

The first release should not attempt to synchronize every workout edit between devices.
Snapshot upload and restore are easier to reason about and match the recovery problem that
started this project.

## Authentication Recommendation

Use email magic links for the first account release, with optional passkey support later.

- No password database is required.
- Email verification is built into the login flow.
- The same verified email can receive release and issue notifications.
- Short-lived access tokens and rotating refresh tokens are required.
- Each installation receives a device record that can be revoked independently.
- Local-only users are never forced to provide an email address.

The account API must rate-limit login requests, expire magic links, prevent token reuse, and
avoid revealing whether an email address is registered.

## Service Placement

Use Cloudflare as the public edge and HomeOps/TrueNAS as the system of record:

`Knurl Android -> Cloudflare Tunnel -> HomeOps Knurl API -> PostgreSQL + snapshot storage`

- Cloudflare Tunnel provides HTTPS without exposing a TrueNAS port.
- PostgreSQL stores account, device, consent, snapshot metadata, and notification records.
- Encrypted snapshot files live in dedicated object/file storage, not database rows.
- The existing feedback relay and social Worker can remain separate during the MVP.
- Tailscale remains an administrator path for maintenance, backups, and private dashboards.

Initial TrueNAS sizing can be modest: two application CPUs, 4 GB RAM, encrypted storage,
and enough capacity for retained snapshots. The important requirement is an off-device backup,
not a large server.

## Snapshot Security

For the first family deployment, use envelope encryption:

- Generate a unique encryption key per account.
- Encrypt each snapshot before writing it to storage.
- Encrypt the account key with a service key held outside the database.
- Store only ciphertext and metadata in the snapshot store.
- Keep the service key in Vaultwarden or a protected deployment secret, never in Git.

This permits login-based restore after reinstall, but the server operator could technically
decrypt snapshots. Full client-side encryption is a later privacy upgrade because it requires
a recoverable user-held key and changes the login/recovery experience.

Each snapshot needs a format version, creation time, app version, byte size, checksum,
encryption metadata, and retention status. The API should retain the latest snapshot plus a
small configurable history and support explicit deletion.

## Core Data Model

- `accounts`: account ID, verified email, status, created/updated timestamps.
- `devices`: device ID, account ID, display name, last seen time, revoked time.
- `sessions`: hashed refresh-token records, expiry, revocation time.
- `consents`: cloud recovery, release email, issue email, analytics, timestamps, policy version.
- `snapshots`: account ID, version, object key, size, checksum, created time, deleted time.
- `feedback_links`: account ID, GitHub issue number, notification state.
- `analytics_events`: anonymous installation ID, event name, app version, timestamp, coarse
  metadata only.

Workout details, weights, plans, and group history belong in the encrypted snapshot and must
not be copied into analytics tables.

## Notifications

Release emails and GitHub issue closure emails should use a transactional email provider.
Self-hosting a mail server is a separate reliability project and is not recommended for the
first release.

- A release record identifies major, migration-required, and ordinary updates.
- A GitHub webhook is verified with a signing secret.
- The feedback relay records the issue number only when the user consents to follow-up email.
- A closed issue produces at most one notification per linked account.
- Every message includes unsubscribe behavior and a link to account preferences.
- Email delivery failures are recorded without repeatedly retrying invalid addresses.

## Analytics Boundary

Analytics must be opt-in and separate from recovery consent. Initial events may include:

- app opened and version adopted
- update check/download/install result
- plan import success/failure category
- recovery snapshot upload/restore success/failure category
- feedback submission success/failure category

Do not send names, email addresses, workout history, exercise names, weights, reps, group
members, or raw recovery data. The app must provide a way to disable analytics and delete the
associated anonymous identifier.

## Decisions Required Before Coding

- Confirm magic-link authentication for MVP, or choose another method.
- Choose the HomeOps API runtime and deployment method on TrueNAS.
- Choose PostgreSQL and encrypted object storage implementation.
- Choose the transactional email provider and sender domain.
- Confirm server-managed envelope encryption is acceptable for trusted family users.
- Set snapshot retention, maximum size, upload frequency, and account deletion rules.
- Decide whether restoring a snapshot replaces local data or offers a merge mode in the first
  release. Replacement-only is recommended initially.
- Approve the analytics event allowlist and consent wording.
- Define the backup restore test and who can administer production data.

## Implementation Order After Approval

1. Deploy a private local API and database with health checks.
2. Add account and device endpoints with magic-link authentication.
3. Add consent and snapshot metadata endpoints.
4. Add encrypted snapshot upload, list, delete, and restore authorization.
5. Add the Android account and restore screens behind an opt-in setting.
6. Add release and GitHub issue notification jobs.
7. Add opt-in analytics and a minimal administrator dashboard.
8. Test backup restore, device revocation, account deletion, and offline behavior before
   inviting users.
