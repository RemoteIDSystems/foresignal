/** RFC 3339 times, as the protocol writes them (`2026-10-11T18:00:00Z`). */

const DATE_TIME =
  /^(\d{4})-(\d{2})-(\d{2})[Tt](\d{2}):(\d{2})(?::(\d{2})(\.\d{1,9})?)?([Zz]|([+-])(\d{2}):(\d{2}))$/;

/** A time the builders accept: a `Date` or a ready RFC 3339 string. */
export type TimeLike = Date | string;

/** True for a proleptic Gregorian leap year. */
export function isLeapYear(year: number): boolean {
  return year % 4 === 0 && (year % 100 !== 0 || year % 400 === 0);
}

/** The number of days of `month` (1-12) in `year`. */
export function daysInMonth(year: number, month: number): number {
  if (month === 2) return isLeapYear(year) ? 29 : 28;
  return [4, 6, 9, 11].includes(month) ? 30 : 31;
}

/** True when the year, month and day name a real calendar date. */
export function isRealDate(year: number, month: number, day: number): boolean {
  return month >= 1 && month <= 12 && day >= 1 && day <= daysInMonth(year, month);
}

/**
 * Parses an RFC 3339 date-time with an offset (seconds optional, as the server accepts) and returns
 * its instant in epoch milliseconds, or `null` when `text` is not a valid date-time.
 */
export function parseRfc3339(text: unknown): number | null {
  if (typeof text !== "string") return null;
  const m = DATE_TIME.exec(text);
  if (!m) return null;
  const [year, month, day, hour, minute] = [1, 2, 3, 4, 5].map((i) => Number(m[i]));
  const second = Number(m[6] ?? 0);
  if (year === undefined || month === undefined || day === undefined || hour === undefined || minute === undefined) {
    return null;
  }
  if (!isRealDate(year, month, day) || hour > 23 || minute > 59 || second > 59) return null;
  let offsetMinutes = 0;
  if (m[9] !== undefined) {
    const oh = Number(m[10]);
    const om = Number(m[11]);
    if (oh > 18 || om > 59 || (oh === 18 && om > 0)) return null;
    offsetMinutes = (oh * 60 + om) * (m[9] === "-" ? -1 : 1);
  }
  const millis = m[7] !== undefined ? Number((m[7].slice(1) + "000").slice(0, 3)) : 0;
  const utc = Date.UTC(year, month - 1, day, hour, minute, second, millis);
  // Date.UTC maps years 0-99 to 1900-1999; correct them.
  const fixed = year < 100 ? new Date(utc).setUTCFullYear(year) : utc;
  return fixed - offsetMinutes * 60_000;
}

/**
 * Formats a time for the wire: a string is returned unchanged (the caller vouches for it), a `Date`
 * becomes UTC with a `Z` (milliseconds only when non-zero), `undefined` and `null` stay as they are.
 */
export function formatTime(value: TimeLike): string;
export function formatTime(value: TimeLike | null | undefined): string | undefined;
export function formatTime(value: TimeLike | null | undefined): string | undefined {
  if (value === null || value === undefined) return undefined;
  if (typeof value === "string") return value;
  if (!(value instanceof Date) || Number.isNaN(value.getTime())) {
    throw new TypeError("Expected a valid Date or an RFC 3339 string");
  }
  const iso = value.toISOString();
  return value.getUTCMilliseconds() === 0 ? iso.replace(/\.\d{3}Z$/, "Z") : iso;
}

/** Formats a calendar date as `YYYY-MM-DD` from a `Date`'s UTC fields. */
export function formatDate(value: Date): string {
  return value.toISOString().slice(0, 10);
}
