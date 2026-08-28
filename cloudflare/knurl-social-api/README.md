# Knurl Social API

Cloudflare Worker and D1 backend for Knurl friend streak sharing and in-app encouragement reactions.

## Endpoints

- `GET /health`
- `POST /v1/register`
- `GET /v1/friend-code`
- `POST /v1/friends/accept`
- `GET /v1/friends`
- `POST /v1/streak`
- `POST /v1/reactions`
- `GET /v1/inbox`
- `POST /v1/inbox/read`
- `POST /v1/push-token`

All `/v1/*` routes except `/v1/register` require:

- `X-Knurl-User-Id`
- `X-Knurl-Device-Id`
- `X-Knurl-Device-Secret`

The device secret is generated and stored by the app. D1 stores only its SHA-256 hash.

## Commands

```powershell
npm install
npm run types
npm run check
npx wrangler d1 migrations apply knurl-social --local
npx wrangler d1 migrations apply knurl-social --remote
npm run deploy
```
