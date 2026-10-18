/**
 * Workspace contexts (docs/Client-Contract.md §5, schema version 1): the type, JSON merge patches (RFC 7396) and
 * the client-side checks that mirror the server's validation, so a client finds a bad context before sending it.
 */
import { ContextValidationError, type ContextFieldError } from "../errors.js";
import { isValidObjectRef } from "./object-ref.js";

/** A workspace context. Null members are dropped when the server stores it. */
export interface WorkspaceContext {
  selectedRefs?: string[];
  focusRef?: string | null;
  timeRange?: { start: string; end?: string | null } | null;
  asOf?: string | null;
  playback?: { state: "playing" | "paused"; rate?: number } | null;
  viewport?: { bbox?: number[]; center?: number[]; radiusKm?: number } | null;
  activeSituationId?: string | null;
  activeHypothesisSetId?: string | null;
  filters?: Record<string, unknown> | null;
  layers?: string[] | null;
}

/** A JSON value. */
export type Json = null | boolean | number | string | Json[] | { [key: string]: Json };

/** The context members of schema version 1. */
export const CONTEXT_MEMBERS: readonly string[] = [
  "selectedRefs", "focusRef", "timeRange", "asOf", "playback", "viewport", "activeSituationId",
  "activeHypothesisSetId", "filters", "layers",
];

/** Most selected references and layers. */
export const MAX_SELECTED_REFS = 200;
/** Most layers. */
export const MAX_LAYERS = 200;
/** Largest serialized context, in bytes. */
export const MAX_CONTEXT_BYTES = 16_384;
/** Largest serialized filters object, in bytes. */
export const MAX_FILTERS_BYTES = 4096;

const ULID = /^[0-9A-HJKMNP-TV-Z]{26}$/;

function isObject(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

/** A deep copy of a JSON value. */
export function cloneJson<T>(value: T): T {
  return value === undefined ? value : (JSON.parse(JSON.stringify(value)) as T);
}

/**
 * Applies a JSON merge patch (RFC 7396) and returns the result; neither argument is modified. Objects merge
 * member by member, `null` removes a member, anything else (arrays included) replaces.
 */
export function applyMergePatch<T = unknown>(target: unknown, patch: unknown): T {
  if (!isObject(patch)) {
    return cloneJson(patch) as T;
  }
  const out: Record<string, unknown> = isObject(target) ? cloneJson(target) : {};
  for (const [key, value] of Object.entries(patch)) {
    if (value === null) {
      delete out[key];
    } else {
      out[key] = applyMergePatch(out[key], value);
    }
  }
  return out as T;
}

/** A copy of `context` without its top-level null members (what the server stores and deep links carry). */
export function stripNullMembers<T extends object>(context: T): T {
  const out: Record<string, unknown> = {};
  for (const [key, value] of Object.entries(context)) {
    if (value !== null && value !== undefined) {
      out[key] = value;
    }
  }
  return out as T;
}

function utf8Length(text: string): number {
  return new TextEncoder().encode(text).length;
}

function isTime(value: unknown): boolean {
  return typeof value === "string" && /^\d{4}-\d{2}-\d{2}T/.test(value) && !Number.isNaN(Date.parse(value));
}

function isSimple(value: unknown): boolean {
  const scalar = (v: unknown): boolean => v === null || ["string", "number", "boolean"].includes(typeof v);
  return scalar(value) || (Array.isArray(value) && value.every(scalar));
}

/** The problems of a context under schema version 1 (empty when valid). */
export function contextProblems(context: unknown, prefix = "context"): ContextFieldError[] {
  const errors: ContextFieldError[] = [];
  if (context === null || context === undefined) {
    return errors;
  }
  if (!isObject(context)) {
    return [{ field: prefix, message: "must be an object" }];
  }
  const p = prefix + ".";
  for (const name of Object.keys(context)) {
    if (!CONTEXT_MEMBERS.includes(name)) {
      errors.push({ field: p + name, message: "is not a workspace context member" });
    }
  }
  const selected = context["selectedRefs"];
  if (selected !== null && selected !== undefined) {
    if (!Array.isArray(selected)) {
      errors.push({ field: p + "selectedRefs", message: "must be an array of references" });
    } else {
      if (selected.length > MAX_SELECTED_REFS) {
        errors.push({ field: p + "selectedRefs", message: `holds more than ${MAX_SELECTED_REFS} references` });
      }
      selected.forEach((r, i) => {
        if (typeof r !== "string" || !isValidObjectRef(r)) {
          errors.push({ field: `${p}selectedRefs[${i}]`, message: "is not a valid object reference" });
        }
      });
    }
  }
  const focus = context["focusRef"];
  if (focus !== null && focus !== undefined && (typeof focus !== "string" || !isValidObjectRef(focus))) {
    errors.push({ field: p + "focusRef", message: "is not a valid object reference" });
  }
  const range = context["timeRange"];
  if (range !== null && range !== undefined) {
    if (!isObject(range)) {
      errors.push({ field: p + "timeRange", message: "must be an object {start, end}" });
    } else {
      if (!isTime(range["start"])) {
        errors.push({ field: p + "timeRange.start", message: "is required (an RFC 3339 date-time)" });
      }
      const end = range["end"];
      if (end !== null && end !== undefined) {
        if (!isTime(end)) {
          errors.push({ field: p + "timeRange.end", message: "must be an RFC 3339 date-time or null" });
        } else if (isTime(range["start"]) && Date.parse(end as string) < Date.parse(range["start"] as string)) {
          errors.push({ field: p + "timeRange.end", message: "must not be before start" });
        }
      }
      for (const name of Object.keys(range)) {
        if (name !== "start" && name !== "end") {
          errors.push({ field: `${p}timeRange.${name}`, message: "is not a time range member" });
        }
      }
    }
  }
  const asOf = context["asOf"];
  if (asOf !== null && asOf !== undefined && !isTime(asOf)) {
    errors.push({ field: p + "asOf", message: "must be an RFC 3339 date-time" });
  }
  const playback = context["playback"];
  if (playback !== null && playback !== undefined) {
    if (!isObject(playback)) {
      errors.push({ field: p + "playback", message: "must be an object {state, rate}" });
    } else {
      if (playback["state"] !== "playing" && playback["state"] !== "paused") {
        errors.push({ field: p + "playback.state", message: "must be playing or paused" });
      }
      const rate = playback["rate"];
      if (rate !== null && rate !== undefined && (typeof rate !== "number" || rate <= 0)) {
        errors.push({ field: p + "playback.rate", message: "must be a number above 0" });
      }
    }
  }
  const viewport = context["viewport"];
  if (viewport !== null && viewport !== undefined) {
    if (!isObject(viewport)) {
      errors.push({ field: p + "viewport", message: "must be an object" });
    } else {
      const hasBbox = viewport["bbox"] !== null && viewport["bbox"] !== undefined;
      const hasCenter = (viewport["center"] !== null && viewport["center"] !== undefined)
        || (viewport["radiusKm"] !== null && viewport["radiusKm"] !== undefined);
      if (hasBbox === hasCenter) {
        errors.push({ field: p + "viewport", message: "must be either bbox or center with radiusKm" });
      } else if (hasBbox) {
        const b = viewport["bbox"];
        const n = Array.isArray(b) && b.length === 4 && b.every((x) => typeof x === "number") ? (b as number[]) : undefined;
        if (!n || n[1]! >= n[3]! || Math.abs(n[0]!) > 180 || Math.abs(n[2]!) > 180 || Math.abs(n[1]!) > 90
          || Math.abs(n[3]!) > 90) {
          errors.push({ field: p + "viewport.bbox", message: "must be [minLon, minLat, maxLon, maxLat]" });
        }
      } else {
        const c = viewport["center"];
        const n = Array.isArray(c) && c.length === 2 && c.every((x) => typeof x === "number") ? (c as number[]) : undefined;
        if (!n || Math.abs(n[0]!) > 180 || Math.abs(n[1]!) > 90) {
          errors.push({ field: p + "viewport.center", message: "must be [lon, lat]" });
        }
        const r = viewport["radiusKm"];
        if (typeof r !== "number" || r <= 0 || r > 20_000) {
          errors.push({ field: p + "viewport.radiusKm", message: "must be above 0 and at most 20000" });
        }
      }
    }
  }
  for (const id of ["activeSituationId", "activeHypothesisSetId"]) {
    const v = context[id];
    if (v !== null && v !== undefined && (typeof v !== "string" || !ULID.test(v))) {
      errors.push({ field: p + id, message: "must be a ULID or null" });
    }
  }
  const filters = context["filters"];
  if (filters !== null && filters !== undefined) {
    if (!isObject(filters)) {
      errors.push({ field: p + "filters", message: "must be an object" });
    } else {
      for (const [name, value] of Object.entries(filters)) {
        if (!isSimple(value)) {
          errors.push({ field: `${p}filters.${name}`, message: "must be a string, number, boolean, null or an array of those" });
        }
      }
      if (utf8Length(JSON.stringify(filters)) > MAX_FILTERS_BYTES) {
        errors.push({ field: p + "filters", message: `is larger than ${MAX_FILTERS_BYTES} bytes` });
      }
    }
  }
  const layers = context["layers"];
  if (layers !== null && layers !== undefined) {
    if (!Array.isArray(layers) || layers.length > MAX_LAYERS) {
      errors.push({ field: p + "layers", message: `must be an array of at most ${MAX_LAYERS} strings` });
    } else if (layers.some((l) => typeof l !== "string" || l.trim() === "" || l.length > 128)) {
      errors.push({ field: p + "layers", message: "each layer is a string of 1 to 128 characters" });
    }
  }
  if (utf8Length(JSON.stringify(context)) > MAX_CONTEXT_BYTES) {
    errors.push({ field: prefix, message: `is larger than ${MAX_CONTEXT_BYTES} bytes` });
  }
  return errors;
}

/**
 * Validates a context under schema version 1.
 *
 * @throws ContextValidationError listing every problem
 */
export function validateContext(context: unknown, prefix = "context"): void {
  const errors = contextProblems(context, prefix);
  if (errors.length > 0) {
    throw new ContextValidationError(errors);
  }
}
