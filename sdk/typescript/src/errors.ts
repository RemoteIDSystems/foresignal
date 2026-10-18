/**
 * Error types of the hand-written SDK layer. They depend only on `fetch` primitives, not on the generated types.
 */

/** An RFC 9457 problem document as Foresignal returns it (`application/problem+json`). */
export interface Problem {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
  /** Stable machine code, e.g. `RESYNC_REQUIRED`. */
  code?: string;
  errors?: { field?: string; message?: string }[];
  [extension: string]: unknown;
}

/** A non-2xx answer from the Foresignal API. */
export class ApiError extends Error {
  /** HTTP status. */
  readonly status: number;
  /** The problem's `code`, when the body was a problem document. */
  readonly code: string | undefined;
  /** The parsed problem document, when there was one. */
  readonly problem: Problem | undefined;
  /** Milliseconds from a `Retry-After` header, when present. */
  readonly retryAfterMs: number | undefined;

  /** Creates the error from its parts. */
  constructor(status: number, problem: Problem | undefined, retryAfterMs: number | undefined, message?: string) {
    super(message ?? problem?.detail ?? problem?.title ?? `HTTP ${status}`);
    this.name = "ApiError";
    this.status = status;
    this.problem = problem;
    this.code = typeof problem?.code === "string" ? problem.code : undefined;
    this.retryAfterMs = retryAfterMs;
  }
}

/**
 * `410 RESYNC_REQUIRED` from `GET /v1/changes`: the cursor is below the purge horizon (heads were purged without
 * tombstones). Re-snapshot with `/v1/records?order=id` and continue from its `watermarkSeq`.
 */
export class ResyncRequiredError extends ApiError {
  /** The purge horizon seq the cursor fell below. */
  readonly purgeHorizon: number | undefined;
  /** The watermark at the time of the refusal. */
  readonly watermarkSeq: number | undefined;

  /** Creates the error from the 410 problem. */
  constructor(problem: Problem | undefined, retryAfterMs: number | undefined) {
    super(410, problem, retryAfterMs, problem?.detail ?? "Resync required: the cursor is below the purge horizon");
    this.name = "ResyncRequiredError";
    this.purgeHorizon = typeof problem?.purgeHorizon === "number" ? problem.purgeHorizon : undefined;
    this.watermarkSeq = typeof problem?.watermarkSeq === "number" ? problem.watermarkSeq : undefined;
  }
}

/** A malformed object reference (docs/Client-Contract.md §2). */
export class ObjectRefError extends Error {
  /** The rejected text. */
  readonly ref: string;

  /** Creates the error for `ref`. */
  constructor(ref: string, message: string) {
    super(`Invalid object reference ${JSON.stringify(ref.length > 80 ? ref.slice(0, 80) + "…" : ref)}: ${message}`);
    this.name = "ObjectRefError";
    this.ref = ref;
  }
}

/** One problem found in a workspace context. */
export interface ContextFieldError {
  field: string;
  message: string;
}

/** A workspace context that breaks the schema version 1 rules (docs/Client-Contract.md §5). */
export class ContextValidationError extends Error {
  /** Every problem found. */
  readonly errors: ContextFieldError[];

  /** Creates the error from its field errors. */
  constructor(errors: ContextFieldError[]) {
    super("The workspace context is not valid: " + errors.map((e) => `${e.field} ${e.message}`).join("; "));
    this.name = "ContextValidationError";
    this.errors = errors;
  }
}

/** Why a deep link was refused. */
export type DeepLinkErrorKind = "version" | "malformed" | "context" | "too-large";

/** A deep link that cannot be encoded or decoded (docs/Client-Contract.md §6). */
export class DeepLinkError extends Error {
  /** The kind of failure. */
  readonly kind: DeepLinkErrorKind;

  /** Creates the error. */
  constructor(kind: DeepLinkErrorKind, message: string) {
    super(message);
    this.name = "DeepLinkError";
    this.kind = kind;
  }
}

/** The stream stopped receiving events and heartbeats for longer than the watchdog allows. */
export class HeartbeatTimeoutError extends Error {
  /** Creates the error. */
  constructor(readonly timeoutMs: number) {
    super(`No event or heartbeat within ${timeoutMs} ms`);
    this.name = "HeartbeatTimeoutError";
  }
}

/**
 * Parses a `Retry-After` header (delta seconds or an HTTP date) into milliseconds.
 *
 * @param value the header value
 * @param now the current time in epoch milliseconds
 */
export function parseRetryAfter(value: string | null | undefined, now: number = Date.now()): number | undefined {
  if (value == null || value.trim() === "") {
    return undefined;
  }
  const trimmed = value.trim();
  if (/^\d+$/.test(trimmed)) {
    return Number(trimmed) * 1000;
  }
  const at = Date.parse(trimmed);
  return Number.isNaN(at) ? undefined : Math.max(0, at - now);
}

/**
 * Builds the error for a non-2xx response: {@link ResyncRequiredError} for `410 RESYNC_REQUIRED`, else
 * {@link ApiError}. The body is read as text and parsed as JSON when possible.
 */
export async function apiErrorFrom(response: Response): Promise<ApiError> {
  let problem: Problem | undefined;
  try {
    const text = await response.text();
    if (text) {
      const parsed: unknown = JSON.parse(text);
      if (parsed && typeof parsed === "object" && !Array.isArray(parsed)) {
        problem = parsed as Problem;
      }
    }
  } catch {
    problem = undefined;
  }
  const retryAfterMs = parseRetryAfter(response.headers.get("Retry-After"));
  if (response.status === 410 && problem?.code === "RESYNC_REQUIRED") {
    return new ResyncRequiredError(problem, retryAfterMs);
  }
  return new ApiError(response.status, problem, retryAfterMs);
}
