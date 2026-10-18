/**
 * Observation periods (EDP §3): `YYYY`, `YYYY-Qn`, `YYYY-MM`, `YYYY-Www`, `YYYY-MM-DD` or an RFC 3339
 * timestamp with an offset. Periods are UTC intervals `[start, end)`; an instant is the degenerate
 * interval `[t, t]`. One series uses one granularity.
 */
import { isRealDate, parseRfc3339 } from "./times.ts";

const YEAR = /^(\d{4})$/;
const QUARTER = /^(\d{4})-Q([1-4])$/;
const MONTH = /^(\d{4})-(0[1-9]|1[0-2])$/;
const WEEK = /^(\d{4})-W(0[1-9]|[1-4]\d|5[0-3])$/;
const DAY = /^(\d{4})-(\d{2})-(\d{2})$/;
const INSTANT = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(:\d{2}(\.\d+)?)?(Z|[+-]\d{2}:\d{2})$/;
const DAY_MS = 86_400_000;

/** A period's granularity. */
export type Granularity = "year" | "quarter" | "month" | "week" | "day" | "instant";

/** A parsed period: granularity, inclusive UTC start and exclusive UTC end, in epoch milliseconds. */
export interface Period {
  /** The granularity. */
  granularity: Granularity;
  /** The inclusive start. */
  start: number;
  /** The exclusive end (equal to the start for an instant). */
  end: number;
}

function utc(year: number, month: number, day: number): number {
  const date = new Date(Date.UTC(2000, month - 1, day));
  date.setUTCFullYear(year);
  return date.getTime();
}

/** The Monday of ISO week 1 of `year`, in epoch milliseconds. */
function isoWeekOneMonday(year: number): number {
  const jan4 = utc(year, 1, 4);
  const weekday = (new Date(jan4).getUTCDay() + 6) % 7; // Monday = 0
  return jan4 - weekday * DAY_MS;
}

/** The number of ISO weeks (52 or 53) of `year`. */
export function isoWeeksInYear(year: number): number {
  return Math.round((isoWeekOneMonday(year + 1) - isoWeekOneMonday(year)) / (7 * DAY_MS));
}

/** Parses an observation's `t`; `null` when it is not an accepted form or not a real date. */
export function parsePeriod(t: unknown): Period | null {
  if (typeof t !== "string") return null;
  let m: RegExpExecArray | null;
  if ((m = YEAR.exec(t))) {
    const year = Number(m[1]);
    return { granularity: "year", start: utc(year, 1, 1), end: utc(year + 1, 1, 1) };
  }
  if ((m = QUARTER.exec(t))) {
    const year = Number(m[1]);
    const first = (Number(m[2]) - 1) * 3 + 1;
    return { granularity: "quarter", start: utc(year, first, 1), end: first === 10 ? utc(year + 1, 1, 1) : utc(year, first + 3, 1) };
  }
  if ((m = MONTH.exec(t))) {
    const year = Number(m[1]);
    const month = Number(m[2]);
    return { granularity: "month", start: utc(year, month, 1), end: month === 12 ? utc(year + 1, 1, 1) : utc(year, month + 1, 1) };
  }
  if ((m = WEEK.exec(t))) {
    const year = Number(m[1]);
    const week = Number(m[2]);
    if (week > isoWeeksInYear(year)) return null;
    const start = isoWeekOneMonday(year) + (week - 1) * 7 * DAY_MS;
    return { granularity: "week", start, end: start + 7 * DAY_MS };
  }
  if ((m = DAY.exec(t))) {
    const [year, month, day] = [Number(m[1]), Number(m[2]), Number(m[3])];
    if (!isRealDate(year, month, day)) return null;
    const start = utc(year, month, day);
    return { granularity: "day", start, end: start + DAY_MS };
  }
  if (INSTANT.test(t)) {
    const instant = parseRfc3339(t);
    return instant === null ? null : { granularity: "instant", start: instant, end: instant };
  }
  return null;
}

/** `YYYY-Qn`. */
export function quarter(year: number, q: number): string {
  if (!Number.isInteger(q) || q < 1 || q > 4) throw new RangeError("A quarter is 1 to 4");
  return `${String(year).padStart(4, "0")}-Q${q}`;
}

/** `YYYY-MM`. */
export function month(year: number, m: number): string {
  if (!Number.isInteger(m) || m < 1 || m > 12) throw new RangeError("A month is 1 to 12");
  return `${String(year).padStart(4, "0")}-${String(m).padStart(2, "0")}`;
}

/** `YYYY-Www` (ISO week). */
export function isoWeek(year: number, week: number): string {
  if (!Number.isInteger(week) || week < 1 || week > isoWeeksInYear(year)) {
    throw new RangeError(`${year} has no ISO week ${week}`);
  }
  return `${String(year).padStart(4, "0")}-W${String(week).padStart(2, "0")}`;
}
