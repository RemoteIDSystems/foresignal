/**
 * Deep links (docs/Client-Contract.md §6): a context travels as `{"v":1,"ws":…,"ctx":{…}}`, compact JSON with the
 * context's null members left out, base64url-encoded without padding, at most 2,048 bytes, carried in a `#fs=`
 * URL fragment. Encoding gives the same payload as the server's `ContextLinks`; decoding checks the payload
 * version and validates the context.
 */
import { DeepLinkError } from "../errors.js";
import { contextProblems, stripNullMembers, type WorkspaceContext } from "./context.js";
import { DEEP_LINK_VERSION } from "./envelope.js";

/** Largest encoded payload, in bytes. */
export const MAX_DEEP_LINK_BYTES = 2048;

/** The URL fragment prefix. */
export const DEEP_LINK_FRAGMENT = "#fs=";

/** An encoded deep link (the shape of the server's `ContextLink`). */
export interface EncodedDeepLink {
  version: number;
  workspaceId: string | null;
  payload: string;
  fragment: string;
  bytes: number;
}

/** A decoded deep link. */
export interface DecodedDeepLink {
  version: number;
  workspaceId: string | null;
  context: WorkspaceContext;
}

function toBase64Url(bytes: Uint8Array): string {
  let binary = "";
  for (const b of bytes) {
    binary += String.fromCharCode(b);
  }
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

function fromBase64Url(text: string): Uint8Array {
  if (!/^[A-Za-z0-9_-]*$/.test(text) || text.length % 4 === 1) {
    throw new Error("not base64url");
  }
  const padded = text.replace(/-/g, "+").replace(/_/g, "/") + "===".slice((text.length + 3) % 4);
  const binary = atob(padded);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) {
    bytes[i] = binary.charCodeAt(i);
  }
  return bytes;
}

/**
 * Encodes a context as a deep link.
 *
 * @param workspaceId the workspace the context belongs to, or null for an ad-hoc context
 * @param context the context (validated first)
 * @throws DeepLinkError `context` when the context is invalid, `too-large` beyond 2,048 bytes
 */
export function encodeDeepLink(workspaceId: string | null, context: WorkspaceContext): EncodedDeepLink {
  const problems = contextProblems(context, "context");
  if (problems.length > 0) {
    throw new DeepLinkError("context", problems.map((e) => `${e.field} ${e.message}`).join("; "));
  }
  const json = JSON.stringify({ v: DEEP_LINK_VERSION, ws: workspaceId, ctx: stripNullMembers(context ?? {}) });
  const payload = toBase64Url(new TextEncoder().encode(json));
  if (payload.length > MAX_DEEP_LINK_BYTES) {
    throw new DeepLinkError("too-large",
      `The encoded context is ${payload.length} bytes; a deep link holds at most ${MAX_DEEP_LINK_BYTES}.`);
  }
  return { version: DEEP_LINK_VERSION, workspaceId, payload, fragment: DEEP_LINK_FRAGMENT + payload, bytes: payload.length };
}

/**
 * Decodes a deep link payload, with or without the `#fs=` prefix (a full URL with that fragment works too).
 *
 * @throws DeepLinkError `malformed`, `version` (unsupported payload version), `context` or `too-large`
 */
export function decodeDeepLink(payloadOrFragment: string): DecodedDeepLink {
  let text = payloadOrFragment ?? "";
  const hash = text.indexOf(DEEP_LINK_FRAGMENT);
  if (hash >= 0) {
    text = text.slice(hash + DEEP_LINK_FRAGMENT.length);
  }
  if (text.trim() === "") {
    throw new DeepLinkError("malformed", "The deep link payload is empty.");
  }
  if (text.length > MAX_DEEP_LINK_BYTES) {
    throw new DeepLinkError("too-large", `The payload is longer than ${MAX_DEEP_LINK_BYTES} bytes.`);
  }
  let doc: unknown;
  try {
    doc = JSON.parse(new TextDecoder("utf-8", { fatal: true }).decode(fromBase64Url(text)));
  } catch {
    throw new DeepLinkError("malformed", "The deep link payload is not base64url JSON.");
  }
  if (typeof doc !== "object" || doc === null || Array.isArray(doc)) {
    throw new DeepLinkError("malformed", 'The deep link payload must be {"v":1,"ws":…,"ctx":{…}}.');
  }
  const record = doc as Record<string, unknown>;
  const v = record["v"];
  if (typeof v !== "number" || !Number.isInteger(v)) {
    throw new DeepLinkError("malformed", 'The deep link payload must be {"v":1,"ws":…,"ctx":{…}}.');
  }
  if (v !== DEEP_LINK_VERSION) {
    throw new DeepLinkError("version", `Unsupported deep link version ${v} (this SDK reads ${DEEP_LINK_VERSION}).`);
  }
  const ctx = record["ctx"] ?? {};
  const problems = contextProblems(ctx, "ctx");
  if (problems.length > 0) {
    throw new DeepLinkError("context", problems.map((e) => `${e.field} ${e.message}`).join("; "));
  }
  const ws = record["ws"];
  return { version: v, workspaceId: typeof ws === "string" ? ws : null, context: ctx as WorkspaceContext };
}
