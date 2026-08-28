const MAX_BODY_BYTES = 64 * 1024;
const MAX_DISPLAY_NAME = 80;
const MAX_REACTION_MESSAGE = 120;
const FRIEND_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
const ALLOWED_REACTIONS = new Set(["thumb", "fire"]);

type RegisterPayload = {
  userId?: unknown;
  deviceId?: unknown;
  deviceSecret?: unknown;
  displayName?: unknown;
};

type FriendAcceptPayload = {
  friendCode?: unknown;
};

type StreakPayload = {
  weekId?: unknown;
  workoutsThisWeek?: unknown;
  consecutiveWeeks?: unknown;
  lastWorkoutDate?: unknown;
};

type ReactionPayload = {
  toUserId?: unknown;
  type?: unknown;
  message?: unknown;
  workoutDate?: unknown;
};

type InboxReadPayload = {
  ids?: unknown;
};

type PushTokenPayload = {
  token?: unknown;
};

type UserRow = {
  id: string;
  display_name: string;
  friend_code: string;
};

type FriendRow = {
  id: string;
  display_name: string;
  friend_code: string;
  week_id: string | null;
  workouts_this_week: number | null;
  consecutive_weeks: number | null;
  last_workout_date: string | null;
  updated_at: string | null;
};

type ReactionRow = {
  id: string;
  from_user_id: string;
  from_display_name: string;
  type: string;
  message: string | null;
  workout_date: string | null;
  created_at: string;
};

class HttpError extends Error {
  status: number;

  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    try {
      const url = new URL(request.url);
      const path = normalizePath(url.pathname);

      if (request.method === "OPTIONS") {
        return new Response(null, { status: 204, headers: commonHeaders() });
      }

      if (request.method === "GET" && path === "/health") {
        return jsonResponse(200, { ok: true, service: "knurl-social-api" });
      }

      if (request.method === "POST" && path === "/v1/register") {
        return jsonResponse(201, await registerDevice(request, env));
      }

      const user = await requireAuth(request, env);

      if (request.method === "GET" && path === "/v1/friend-code") {
        return jsonResponse(200, { ok: true, user });
      }
      if (request.method === "POST" && path === "/v1/friends/accept") {
        return jsonResponse(200, await acceptFriend(request, env, user));
      }
      if (request.method === "GET" && path === "/v1/friends") {
        return jsonResponse(200, await listFriends(env, user));
      }
      if (request.method === "POST" && path === "/v1/streak") {
        return jsonResponse(200, await upsertStreak(request, env, user));
      }
      if (request.method === "POST" && path === "/v1/reactions") {
        return jsonResponse(201, await createReaction(request, env, user));
      }
      if (request.method === "GET" && path === "/v1/inbox") {
        return jsonResponse(200, await listInbox(env, user));
      }
      if (request.method === "POST" && path === "/v1/inbox/read") {
        return jsonResponse(200, await markInboxRead(request, env, user));
      }
      if (request.method === "POST" && path === "/v1/push-token") {
        return jsonResponse(200, await savePushToken(request, env, user));
      }

      return jsonResponse(404, { ok: false, error: "Unknown route." });
    } catch (error) {
      const status = error instanceof HttpError ? error.status : 500;
      const message = error instanceof Error ? error.message : "Unexpected error.";
      console.log(JSON.stringify({ level: "error", status, message }));
      return jsonResponse(status, { ok: false, error: message });
    }
  }
} satisfies ExportedHandler<Env>;

async function registerDevice(request: Request, env: Env): Promise<unknown> {
  const payload = await readJsonPayload<RegisterPayload>(request);
  const userId = validId(payload.userId) || crypto.randomUUID();
  const deviceId = validId(payload.deviceId) || crypto.randomUUID();
  const deviceSecret = requiredSecret(payload.deviceSecret);
  const displayName = cleanDisplayName(payload.displayName);
  const now = new Date().toISOString();

  const existing = await env.SOCIAL_DB.prepare(
    "SELECT id, display_name, friend_code FROM users WHERE id = ?"
  ).bind(userId).first<UserRow>();
  const friendCode = existing?.friend_code || await uniqueFriendCode(env);

  await env.SOCIAL_DB.batch([
    env.SOCIAL_DB.prepare(
      `INSERT INTO users (id, display_name, friend_code, created_at, updated_at)
       VALUES (?, ?, ?, ?, ?)
       ON CONFLICT(id) DO UPDATE SET
         display_name = excluded.display_name,
         updated_at = excluded.updated_at`
    ).bind(userId, displayName, friendCode, now, now),
    env.SOCIAL_DB.prepare(
      `INSERT INTO devices (id, user_id, device_public_id, device_secret_hash, created_at, updated_at, last_seen_at)
       VALUES (?, ?, ?, ?, ?, ?, ?)
       ON CONFLICT(id) DO UPDATE SET
         user_id = excluded.user_id,
         device_public_id = excluded.device_public_id,
         device_secret_hash = excluded.device_secret_hash,
         updated_at = excluded.updated_at,
         last_seen_at = excluded.last_seen_at`
    ).bind(deviceId, userId, deviceId, await sha256Hex(deviceSecret), now, now, now)
  ]);

  return {
    ok: true,
    user: {
      id: userId,
      displayName,
      friendCode
    },
    device: {
      id: deviceId
    }
  };
}

async function requireAuth(request: Request, env: Env): Promise<UserRow> {
  const userId = headerText(request, "X-Knurl-User-Id");
  const deviceId = headerText(request, "X-Knurl-Device-Id");
  const deviceSecret = headerText(request, "X-Knurl-Device-Secret");
  if (!validId(userId) || !validId(deviceId) || !deviceSecret) {
    throw new HttpError(401, "Missing Knurl device credentials.");
  }

  const row = await env.SOCIAL_DB.prepare(
    `SELECT users.id, users.display_name, users.friend_code, devices.device_secret_hash
     FROM devices
     JOIN users ON users.id = devices.user_id
     WHERE devices.id = ? AND devices.user_id = ?`
  ).bind(deviceId, userId).first<UserRow & { device_secret_hash: string }>();

  if (!row) throw new HttpError(401, "Invalid Knurl device credentials.");
  const incomingHash = await sha256Hex(deviceSecret);
  if (!constantTimeEqual(incomingHash, row.device_secret_hash)) {
    throw new HttpError(401, "Invalid Knurl device credentials.");
  }

  await env.SOCIAL_DB.prepare(
    "UPDATE devices SET last_seen_at = ?, updated_at = ? WHERE id = ?"
  ).bind(new Date().toISOString(), new Date().toISOString(), deviceId).run();

  return {
    id: row.id,
    display_name: row.display_name,
    friend_code: row.friend_code
  };
}

async function acceptFriend(request: Request, env: Env, user: UserRow): Promise<unknown> {
  const payload = await readJsonPayload<FriendAcceptPayload>(request);
  const friendCode = toText(payload.friendCode).trim().toUpperCase();
  if (!/^[A-Z0-9]{8}$/.test(friendCode)) throw new HttpError(400, "Friend code is invalid.");

  const friend = await env.SOCIAL_DB.prepare(
    "SELECT id, display_name, friend_code FROM users WHERE friend_code = ?"
  ).bind(friendCode).first<UserRow>();
  if (!friend) throw new HttpError(404, "Friend code was not found.");
  if (friend.id === user.id) throw new HttpError(400, "You cannot add yourself as a friend.");

  const [userA, userB] = [user.id, friend.id].sort();
  const id = `${userA}:${userB}`;
  const now = new Date().toISOString();
  await env.SOCIAL_DB.prepare(
    `INSERT INTO friendships (id, user_a_id, user_b_id, status, created_at, updated_at)
     VALUES (?, ?, ?, 'accepted', ?, ?)
     ON CONFLICT(id) DO UPDATE SET status = 'accepted', updated_at = excluded.updated_at`
  ).bind(id, userA, userB, now, now).run();

  return { ok: true, friend: mapFriend(friend) };
}

async function listFriends(env: Env, user: UserRow): Promise<unknown> {
  const rows = await env.SOCIAL_DB.prepare(
    `SELECT users.id, users.display_name, users.friend_code,
            streak_summaries.week_id,
            streak_summaries.workouts_this_week,
            streak_summaries.consecutive_weeks,
            streak_summaries.last_workout_date,
            streak_summaries.updated_at
     FROM friendships
     JOIN users ON users.id = CASE
       WHEN friendships.user_a_id = ? THEN friendships.user_b_id
       ELSE friendships.user_a_id
     END
     LEFT JOIN streak_summaries ON streak_summaries.user_id = users.id
     WHERE friendships.status = 'accepted'
       AND (friendships.user_a_id = ? OR friendships.user_b_id = ?)
     ORDER BY lower(users.display_name)`
  ).bind(user.id, user.id, user.id).all<FriendRow>();

  return {
    ok: true,
    friends: (rows.results || []).map((row) => ({
      id: row.id,
      displayName: row.display_name,
      friendCode: row.friend_code,
      streak: {
        weekId: row.week_id || "",
        workoutsThisWeek: Number(row.workouts_this_week || 0),
        consecutiveWeeks: Number(row.consecutive_weeks || 0),
        lastWorkoutDate: row.last_workout_date || "",
        updatedAt: row.updated_at || ""
      }
    }))
  };
}

async function upsertStreak(request: Request, env: Env, user: UserRow): Promise<unknown> {
  const payload = await readJsonPayload<StreakPayload>(request);
  const weekId = isoWeekId(payload.weekId);
  const workoutsThisWeek = clampInteger(payload.workoutsThisWeek, 0, 14);
  const consecutiveWeeks = clampInteger(payload.consecutiveWeeks, 0, 5200);
  const lastWorkoutDate = optionalIsoDate(payload.lastWorkoutDate);
  const now = new Date().toISOString();

  await env.SOCIAL_DB.prepare(
    `INSERT INTO streak_summaries (user_id, week_id, workouts_this_week, consecutive_weeks, last_workout_date, updated_at)
     VALUES (?, ?, ?, ?, ?, ?)
     ON CONFLICT(user_id) DO UPDATE SET
       week_id = excluded.week_id,
       workouts_this_week = excluded.workouts_this_week,
       consecutive_weeks = excluded.consecutive_weeks,
       last_workout_date = excluded.last_workout_date,
       updated_at = excluded.updated_at`
  ).bind(user.id, weekId, workoutsThisWeek, consecutiveWeeks, lastWorkoutDate, now).run();

  return {
    ok: true,
    streak: { weekId, workoutsThisWeek, consecutiveWeeks, lastWorkoutDate, updatedAt: now }
  };
}

async function createReaction(request: Request, env: Env, user: UserRow): Promise<unknown> {
  const payload = await readJsonPayload<ReactionPayload>(request);
  const toUserId = validId(payload.toUserId);
  if (!toUserId) throw new HttpError(400, "Reaction recipient is required.");
  if (toUserId === user.id) throw new HttpError(400, "You cannot react to yourself.");
  await requireFriendship(env, user.id, toUserId);

  const type = toText(payload.type).trim();
  if (!ALLOWED_REACTIONS.has(type)) throw new HttpError(400, "Reaction type is not supported.");
  const message = toText(payload.message).trim().slice(0, MAX_REACTION_MESSAGE);
  const workoutDate = optionalIsoDate(payload.workoutDate);
  const id = crypto.randomUUID();
  const now = new Date().toISOString();

  await env.SOCIAL_DB.prepare(
    `INSERT INTO reactions (id, from_user_id, to_user_id, type, message, workout_date, created_at)
     VALUES (?, ?, ?, ?, ?, ?, ?)`
  ).bind(id, user.id, toUserId, type, message || null, workoutDate, now).run();

  return { ok: true, reaction: { id, toUserId, type, message, workoutDate, createdAt: now } };
}

async function listInbox(env: Env, user: UserRow): Promise<unknown> {
  const rows = await env.SOCIAL_DB.prepare(
    `SELECT reactions.id, reactions.from_user_id, users.display_name AS from_display_name,
            reactions.type, reactions.message, reactions.workout_date, reactions.created_at
     FROM reactions
     JOIN users ON users.id = reactions.from_user_id
     WHERE reactions.to_user_id = ? AND reactions.read_at IS NULL
     ORDER BY reactions.created_at DESC
     LIMIT 50`
  ).bind(user.id).all<ReactionRow>();

  return {
    ok: true,
    reactions: (rows.results || []).map((row) => ({
      id: row.id,
      fromUserId: row.from_user_id,
      fromDisplayName: row.from_display_name,
      type: row.type,
      message: row.message || "",
      workoutDate: row.workout_date || "",
      createdAt: row.created_at
    }))
  };
}

async function markInboxRead(request: Request, env: Env, user: UserRow): Promise<unknown> {
  const payload = await readJsonPayload<InboxReadPayload>(request);
  const ids = Array.isArray(payload.ids) ? payload.ids.map(validId).filter((id): id is string => !!id) : [];
  if (!ids.length) return { ok: true, updated: 0 };

  const now = new Date().toISOString();
  let updated = 0;
  for (const id of ids.slice(0, 50)) {
    const result = await env.SOCIAL_DB.prepare(
      "UPDATE reactions SET read_at = ? WHERE id = ? AND to_user_id = ? AND read_at IS NULL"
    ).bind(now, id, user.id).run();
    updated += result.meta.changes || 0;
  }
  return { ok: true, updated };
}

async function savePushToken(request: Request, env: Env, user: UserRow): Promise<unknown> {
  const payload = await readJsonPayload<PushTokenPayload>(request);
  const token = toText(payload.token).trim().slice(0, 4096);
  const deviceId = headerText(request, "X-Knurl-Device-Id");
  await env.SOCIAL_DB.prepare(
    "UPDATE devices SET fcm_token = ?, updated_at = ? WHERE id = ? AND user_id = ?"
  ).bind(token || null, new Date().toISOString(), deviceId, user.id).run();
  return { ok: true };
}

async function requireFriendship(env: Env, userId: string, friendId: string): Promise<void> {
  const [userA, userB] = [userId, friendId].sort();
  const row = await env.SOCIAL_DB.prepare(
    "SELECT id FROM friendships WHERE id = ? AND status = 'accepted'"
  ).bind(`${userA}:${userB}`).first<{ id: string }>();
  if (!row) throw new HttpError(403, "Users are not friends.");
}

async function readJsonPayload<T>(request: Request): Promise<T> {
  const contentType = request.headers.get("Content-Type") || "";
  if (!contentType.toLowerCase().includes("application/json")) {
    throw new HttpError(415, "Content-Type must be application/json.");
  }

  const contentLength = Number(request.headers.get("Content-Length") || "0");
  if (Number.isFinite(contentLength) && contentLength > MAX_BODY_BYTES) {
    throw new HttpError(413, "Request body is too large.");
  }

  const bytes = await request.arrayBuffer();
  if (bytes.byteLength > MAX_BODY_BYTES) {
    throw new HttpError(413, "Request body is too large.");
  }

  try {
    return JSON.parse(new TextDecoder().decode(bytes)) as T;
  } catch {
    throw new HttpError(400, "Invalid JSON payload.");
  }
}

async function uniqueFriendCode(env: Env): Promise<string> {
  for (let i = 0; i < 12; i++) {
    const code = randomFriendCode();
    const existing = await env.SOCIAL_DB.prepare("SELECT id FROM users WHERE friend_code = ?").bind(code).first();
    if (!existing) return code;
  }
  throw new HttpError(500, "Could not allocate friend code.");
}

function randomFriendCode(): string {
  const bytes = new Uint8Array(8);
  crypto.getRandomValues(bytes);
  return Array.from(bytes, (byte) => FRIEND_CODE_CHARS[byte % FRIEND_CODE_CHARS.length]).join("");
}

function mapFriend(row: UserRow): unknown {
  return {
    id: row.id,
    displayName: row.display_name,
    friendCode: row.friend_code
  };
}

function cleanDisplayName(value: unknown): string {
  const cleaned = toText(value).replace(/\s+/g, " ").trim().slice(0, MAX_DISPLAY_NAME);
  if (!cleaned) throw new HttpError(400, "Display name is required.");
  return cleaned;
}

function validId(value: unknown): string | null {
  const text = toText(value).trim();
  return /^[A-Za-z0-9:_-]{8,96}$/.test(text) ? text : null;
}

function requiredSecret(value: unknown): string {
  const text = toText(value).trim();
  if (text.length < 24 || text.length > 256) throw new HttpError(400, "Device secret is invalid.");
  return text;
}

function isoWeekId(value: unknown): string {
  const text = toText(value).trim();
  if (/^\d{4}-W\d{2}$/.test(text)) return text;
  throw new HttpError(400, "weekId must use YYYY-W## format.");
}

function optionalIsoDate(value: unknown): string | null {
  const text = toText(value).trim();
  if (!text) return null;
  if (/^\d{4}-\d{2}-\d{2}$/.test(text)) return text;
  throw new HttpError(400, "Date must use YYYY-MM-DD format.");
}

function clampInteger(value: unknown, min: number, max: number): number {
  const parsed = Number(value);
  if (!Number.isInteger(parsed)) throw new HttpError(400, "Expected an integer value.");
  return Math.min(max, Math.max(min, parsed));
}

async function sha256Hex(input: string): Promise<string> {
  const digest = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(input));
  return Array.from(new Uint8Array(digest), (byte) => byte.toString(16).padStart(2, "0")).join("");
}

function constantTimeEqual(a: string, b: string): boolean {
  const max = Math.max(a.length, b.length);
  let diff = a.length ^ b.length;
  for (let i = 0; i < max; i++) {
    diff |= (a.charCodeAt(i) || 0) ^ (b.charCodeAt(i) || 0);
  }
  return diff === 0;
}

function headerText(request: Request, name: string): string {
  return (request.headers.get(name) || "").trim();
}

function toText(value: unknown): string {
  return value === null || value === undefined ? "" : String(value);
}

function normalizePath(pathname: string): string {
  const normalized = pathname.replace(/\/+$/, "");
  return normalized || "/";
}

function jsonResponse(status: number, body: unknown): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: commonHeaders({ "Content-Type": "application/json; charset=utf-8" })
  });
}

function commonHeaders(extra: Record<string, string> = {}): Headers {
  return new Headers({
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Headers": "Content-Type, X-Knurl-User-Id, X-Knurl-Device-Id, X-Knurl-Device-Secret",
    "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
    "Cache-Control": "no-store",
    "X-Content-Type-Options": "nosniff",
    ...extra
  });
}
