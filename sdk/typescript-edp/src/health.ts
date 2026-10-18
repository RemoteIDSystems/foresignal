/**
 * Status reporting for pull providers (EDP §3, `status` items). A push provider calls
 * `PushClient.reportStatus`; a pull provider records health on a {@link StatusBoard} and the pull
 * handler puts every changed status at the front of the next page it serves. Health is kept per
 * scope (the source, or one stream), and only the latest value of each scope is delivered.
 */
import { status as statusItem } from "./items.ts";
import type { StatusItem } from "./items.ts";
import type { Health } from "./protocol.ts";

const SOURCE = "\u0000source";

function same(a: StatusItem | undefined, b: StatusItem): boolean {
  return a !== undefined && JSON.stringify(a) === JSON.stringify(b);
}

/** Latest health per scope, delivered once per change. */
export class StatusBoard {
  private readonly latest = new Map<string, StatusItem>();
  private readonly pending = new Map<string, StatusItem>();

  /**
   * Records the health of the source (no `stream`) or of one stream. A value equal to the current one
   * is not delivered again.
   *
   * @throws RangeError for an unknown health value
   */
  report(health: Health, message?: string, options: { stream?: string } = {}): void {
    const item = statusItem(health, message, options);
    const scope = options.stream ?? SOURCE;
    if (same(this.latest.get(scope), item)) return;
    this.latest.set(scope, item);
    this.pending.set(scope, item);
  }

  /** Records `ok` (shorthand for {@link report}). */
  ok(options: { stream?: string; message?: string } = {}): void {
    this.report("ok", options.message, options.stream === undefined ? {} : { stream: options.stream });
  }

  /** The latest status item of every scope reported so far. */
  current(): StatusItem[] {
    return [...this.latest.values()].map((i) => ({ ...i }));
  }

  /** Returns the status items changed since the last drain, and forgets them. */
  drain(): StatusItem[] {
    const out = [...this.pending.values()].map((i) => ({ ...i }));
    this.pending.clear();
    return out;
  }

  /** Puts drained items back (when their page could not be served), unless a newer value was reported meanwhile. */
  requeue(items: readonly StatusItem[]): void {
    for (const item of items) {
      const scope = item.stream ?? SOURCE;
      if (!this.pending.has(scope) && same(this.latest.get(scope), item)) this.pending.set(scope, item);
    }
  }
}
