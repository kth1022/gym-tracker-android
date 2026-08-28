CREATE TABLE IF NOT EXISTS users (
  id TEXT PRIMARY KEY,
  display_name TEXT NOT NULL,
  friend_code TEXT NOT NULL UNIQUE,
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS devices (
  id TEXT PRIMARY KEY,
  user_id TEXT NOT NULL,
  device_public_id TEXT NOT NULL,
  device_secret_hash TEXT NOT NULL,
  fcm_token TEXT,
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,
  last_seen_at TEXT,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_devices_user_public_id ON devices(user_id, device_public_id);

CREATE TABLE IF NOT EXISTS friendships (
  id TEXT PRIMARY KEY,
  user_a_id TEXT NOT NULL,
  user_b_id TEXT NOT NULL,
  status TEXT NOT NULL,
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,
  FOREIGN KEY (user_a_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (user_b_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_friendships_user_a ON friendships(user_a_id, status);
CREATE INDEX IF NOT EXISTS idx_friendships_user_b ON friendships(user_b_id, status);

CREATE TABLE IF NOT EXISTS streak_summaries (
  user_id TEXT PRIMARY KEY,
  week_id TEXT NOT NULL,
  workouts_this_week INTEGER NOT NULL,
  consecutive_weeks INTEGER NOT NULL,
  last_workout_date TEXT,
  updated_at TEXT NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reactions (
  id TEXT PRIMARY KEY,
  from_user_id TEXT NOT NULL,
  to_user_id TEXT NOT NULL,
  type TEXT NOT NULL,
  message TEXT,
  workout_date TEXT,
  created_at TEXT NOT NULL,
  read_at TEXT,
  FOREIGN KEY (from_user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (to_user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_reactions_to_user_unread ON reactions(to_user_id, read_at, created_at);
CREATE INDEX IF NOT EXISTS idx_reactions_from_user ON reactions(from_user_id, created_at);
