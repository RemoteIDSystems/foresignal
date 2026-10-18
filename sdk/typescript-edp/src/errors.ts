/** Error and result types shared by the validator, the push client and the pull helper. */

/** One validation problem: a JSON Pointer into the checked document and a message. */
export interface SchemaError {
  /** JSON Pointer (RFC 6901); empty for the root. */
  path: string;
  /** What is wrong. */
  message: string;
}

/**
 * Why one item was refused (OpenAPI `EdpItemRejection`). `reason` is one of the server's stable
 * codes: `SCHEMA_INVALID`, `UNKNOWN_STREAM`, `ITEM_TYPE_MISMATCH`, `PAYLOAD_INVALID`,
 * `GEOMETRY_INVALID`, `PERIOD_INVALID`, `GRANULARITY_MIXED`, `FORECAST_INVALID`,
 * `MISSING_OBSERVED_AT`, `OBSERVED_AT_IN_FUTURE`, `LIMIT_EXCEEDED` (or a server-only code).
 */
export interface ItemRejection {
  /** The item's position. */
  index: number;
  /** Its stream, when it named one. */
  stream: string | null;
  /** Its id, when it had one. */
  id: string | null;
  /** The rejection code. */
  reason: string;
  /** The individual problems. */
  schemaErrors: SchemaError[];
}

/** Creates a rejection with one message at the item root. */
export function rejection(index: number, stream: string | null, id: string | null, reason: string,
  message: string): ItemRejection {
  return { index, stream, id, reason, schemaErrors: [{ path: "", message }] };
}

/** Base class of this library's errors. */
export class EdpError extends Error {
  /** Creates the error. */
  constructor(message: string) {
    super(message);
    this.name = new.target.name;
  }
}

/** A manifest failed validation; `errors` holds every problem. */
export class ManifestInvalidError extends EdpError {
  /** Every problem found. */
  readonly errors: SchemaError[];
  /** True when the protocol minor is above what the validator supports. */
  readonly protocolUnsupported: boolean;

  /** Creates the error from the validator's findings. */
  constructor(errors: SchemaError[], protocolUnsupported: boolean) {
    const summary = errors.slice(0, 5).map((e) => `${e.path || "/"}: ${e.message}`).join("; ");
    const more = errors.length > 5 ? ` (+${errors.length - 5} more)` : "";
    super(`The EDP manifest is invalid: ${summary}${more}`);
    this.errors = errors;
    this.protocolUnsupported = protocolUnsupported;
  }
}

/** A stream's `payloadSchema` breaks the EDP §7 rules or cannot be compiled. */
export class PayloadSchemaError extends EdpError {
  /** Every problem, with paths relative to the schema root. */
  readonly errors: SchemaError[];

  /** Creates the error. */
  constructor(errors: SchemaError[]) {
    super(errors.map((e) => `${e.path || "/"}: ${e.message}`).join("; "));
    this.errors = errors;
  }
}

/** One item alone does not fit in a batch of the configured byte limit. */
export class ItemTooLargeError extends EdpError {
  /** The item's position. */
  readonly index: number;
  /** The bytes it needs in a batch. */
  readonly size: number;
  /** The configured limit. */
  readonly limit: number;

  /** Creates the error. */
  constructor(index: number, size: number, limit: number) {
    super(`Item ${index} needs ${size} bytes in a batch; the limit is ${limit}`);
    this.index = index;
    this.size = size;
    this.limit = limit;
  }
}

/** A pull cursor could not be decoded (unknown, expired or tampered with). */
export class CursorError extends EdpError {}

/** A body is not a readable batch (malformed JSON, too deep, not an object, too many items). */
export class BatchFormatError extends EdpError {}
