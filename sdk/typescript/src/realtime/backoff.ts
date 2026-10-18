/**
 * Jittered exponential reconnect backoff shared by the realtime helpers: 1 s doubling to 60 s, each delay
 * multiplied by a random factor in [0.8, 1.2) and clamped to [initial, max], never below a server-requested
 * minimum (`retry:` or `Retry-After`).
 */

/** Backoff settings. */
export interface BackoffOptions {
  /** First delay in milliseconds (default 1000). */
  initialMs?: number;
  /** Largest delay in milliseconds (default 60000). */
  maxMs?: number;
  /** Random source in [0, 1) (default `Math.random`). */
  random?: () => number;
}

/** Computes successive reconnect delays. */
export class Backoff {
  /** First delay. */
  readonly initialMs: number;
  /** Largest delay. */
  readonly maxMs: number;
  private readonly random: () => number;
  private attemptCount = 0;

  /** Creates a backoff. */
  constructor(options: BackoffOptions = {}) {
    this.initialMs = options.initialMs ?? 1000;
    this.maxMs = options.maxMs ?? 60_000;
    this.random = options.random ?? Math.random;
  }

  /** Delays handed out since the last {@link reset}. */
  get attempt(): number {
    return this.attemptCount;
  }

  /**
   * The next delay in milliseconds.
   *
   * @param minimumMs a server-requested floor (`retry:` or `Retry-After`); it may exceed {@link maxMs}
   */
  next(minimumMs = 0): number {
    const base = Math.min(this.maxMs, this.initialMs * 2 ** Math.min(this.attemptCount, 30));
    this.attemptCount++;
    const jittered = Math.min(this.maxMs, Math.max(this.initialMs, base * (0.8 + 0.4 * this.random())));
    return Math.max(Math.round(jittered), minimumMs);
  }

  /** Starts over at {@link initialMs} (after a connection that worked). */
  reset(): void {
    this.attemptCount = 0;
  }
}
