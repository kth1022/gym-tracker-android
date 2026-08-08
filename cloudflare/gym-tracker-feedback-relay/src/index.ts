const MAX_BODY_BYTES = 1024 * 1024;
const MAX_TITLE_LENGTH = 180;
const MAX_ISSUE_BODY_LENGTH = 60000;
const DEFAULT_RATE_LIMIT_MAX = 12;
const DEFAULT_RATE_LIMIT_WINDOW_SECONDS = 60 * 60;

const ALLOWED_LABELS = new Set([
  "user feedback",
  "feature request",
  "enhancement",
  "bug",
  "data recovery",
  "feedback"
]);

type FeedbackPayload = {
  type?: unknown;
  repo?: unknown;
  title?: unknown;
  body?: unknown;
  labels?: unknown;
};

type ValidIssue = {
  title: string;
  body: string;
  labels: string[];
};

type RuntimeEnv = Env & {
  GITHUB_TOKEN?: string;
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

      if (request.method === "OPTIONS") {
        return new Response(null, { status: 204, headers: commonHeaders() });
      }

      if (request.method === "GET" && normalizePath(url.pathname) === "/health") {
        return jsonResponse(200, { ok: true, service: "gym-tracker-feedback-relay" });
      }

      if (request.method !== "POST" || normalizePath(url.pathname) !== "/api/gym-tracker/feedback") {
        return jsonResponse(404, { ok: false, error: "Unknown route." });
      }

      const clientIp = request.headers.get("CF-Connecting-IP") || "unknown";
      await enforceRateLimit(env, clientIp);

      const payload = await readJsonPayload(request);
      const issue = validateIssuePayload(payload, env.GITHUB_REPO);
      const githubIssue = await createGitHubIssue(env, issue);

      return jsonResponse(201, {
        ok: true,
        issue: {
          number: githubIssue.number,
          url: githubIssue.html_url
        }
      });
    } catch (error) {
      const status = error instanceof HttpError ? error.status : 500;
      const message = error instanceof Error ? error.message : "Unexpected error.";
      console.log(JSON.stringify({ level: "error", status, message }));
      return jsonResponse(status, { ok: false, error: message });
    }
  }
} satisfies ExportedHandler<Env>;

function normalizePath(pathname: string): string {
  const normalized = pathname.replace(/\/+$/, "");
  return normalized || "/";
}

async function readJsonPayload(request: Request): Promise<FeedbackPayload> {
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
    return JSON.parse(new TextDecoder().decode(bytes)) as FeedbackPayload;
  } catch {
    throw new HttpError(400, "Invalid JSON payload.");
  }
}

function validateIssuePayload(payload: FeedbackPayload, expectedRepo: string): ValidIssue {
  if (!payload || payload.type !== "GymTrackerGitHubIssueSubmitV1") {
    throw new HttpError(400, "Invalid Gym Tracker feedback payload.");
  }
  if (toText(payload.repo) !== expectedRepo) {
    throw new HttpError(400, "Invalid feedback target repository.");
  }

  const title = toText(payload.title).trim();
  const body = toText(payload.body).trim();
  if (title.length < 5 || title.length > MAX_TITLE_LENGTH) {
    throw new HttpError(400, "Feedback title must be 5 to 180 characters.");
  }
  if (body.length < 5 || body.length > MAX_ISSUE_BODY_LENGTH) {
    throw new HttpError(400, "Feedback body must be 5 to 60000 characters.");
  }

  const labels = labelsFromPayload(payload.labels);
  return { title, body, labels };
}

function labelsFromPayload(value: unknown): string[] {
  const rawLabels = Array.isArray(value) ? value : [];
  const labels = rawLabels
    .map((label) => toText(label).trim())
    .filter((label) => ALLOWED_LABELS.has(label));

  if (!labels.includes("user feedback")) labels.unshift("user feedback");
  if (labels.includes("feature request") && !labels.includes("enhancement")) labels.push("enhancement");

  return [...new Set(labels)];
}

async function enforceRateLimit(env: Env, clientIp: string): Promise<void> {
  const maxRequests = positiveInt(env.RATE_LIMIT_MAX_REQUESTS, DEFAULT_RATE_LIMIT_MAX);
  const windowSeconds = positiveInt(env.RATE_LIMIT_WINDOW_SECONDS, DEFAULT_RATE_LIMIT_WINDOW_SECONDS);
  const now = Math.floor(Date.now() / 1000);
  const key = `feedback:${clientIp}`;

  const existing = await env.FEEDBACK_RATE_LIMIT.get(key, "json") as { count?: number; resetAt?: number } | null;
  const resetAt = typeof existing?.resetAt === "number" && existing.resetAt > now
    ? existing.resetAt
    : now + windowSeconds;
  const count = resetAt === existing?.resetAt ? Number(existing?.count || 0) : 0;

  if (count >= maxRequests) {
    throw new HttpError(429, "Gym Tracker feedback rate limit reached. Try again later.");
  }

  await env.FEEDBACK_RATE_LIMIT.put(
    key,
    JSON.stringify({ count: count + 1, resetAt }),
    { expirationTtl: Math.max(60, resetAt - now) }
  );
}

async function createGitHubIssue(env: RuntimeEnv, issue: ValidIssue): Promise<{ number: number; html_url: string }> {
  const token = toText(env.GITHUB_TOKEN).trim();
  if (!token) {
    throw new HttpError(503, "GitHub issue token is not configured.");
  }

  const response = await fetch(`https://api.github.com/repos/${env.GITHUB_REPO}/issues`, {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${token}`,
      "Accept": "application/vnd.github+json",
      "X-GitHub-Api-Version": "2022-11-28",
      "User-Agent": "GymTrackerFeedbackRelay",
      "Content-Type": "application/json; charset=utf-8"
    },
    body: JSON.stringify({
      title: issue.title,
      body: issue.body,
      labels: issue.labels
    })
  });

  const result = await response.json() as { number?: number; html_url?: string; message?: string };
  if (!response.ok) {
    throw new HttpError(502, `GitHub issue creation failed (${response.status}): ${toText(result.message).slice(0, 240)}`);
  }

  return {
    number: Number(result.number || 0),
    html_url: toText(result.html_url)
  };
}

function positiveInt(value: string | undefined, fallback: number): number {
  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : fallback;
}

function toText(value: unknown): string {
  return value === null || value === undefined ? "" : String(value);
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
    "Access-Control-Allow-Headers": "Content-Type",
    "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
    "Cache-Control": "no-store",
    "X-Content-Type-Options": "nosniff",
    ...extra
  });
}
