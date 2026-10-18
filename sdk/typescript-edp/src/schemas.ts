/**
 * The exported EDP 1.0 JSON Schemas (embedded from docs/protocol/edp-1.0 by scripts/sync-schemas.mjs)
 * and validation against them with Ajv's JSON Schema 2020-12 dialect.
 *
 * `format` keywords are annotations in 2020-12, as on the server; time strings are checked by the
 * item validator itself.
 */
import { Ajv2020 } from "ajv/dist/2020.js";
import type { ErrorObject, ValidateFunction } from "ajv/dist/2020.js";
import type { SchemaError } from "./errors.ts";
import { EXPORTED_SCHEMAS } from "./generated/schemas.ts";
import { SCHEMA_NAMES } from "./protocol.ts";
import type { SchemaName } from "./protocol.ts";

const MAX_MESSAGE = 500;

/** Creates an Ajv 2020-12 instance configured as the library uses it. */
export function createAjv(): Ajv2020 {
  return new Ajv2020({ strict: false, allErrors: true, validateFormats: false, addUsedSchema: false });
}

let shared: Ajv2020 | undefined;
const compiled = new Map<string, ValidateFunction>();

/** Returns a deep copy of the exported schema `name`. */
export function loadSchema(name: SchemaName): Record<string, unknown> {
  const schema = EXPORTED_SCHEMAS[name];
  if (!(SCHEMA_NAMES as readonly string[]).includes(name) || schema === undefined) {
    throw new RangeError(`Unknown EDP schema '${String(name)}'`);
  }
  return structuredClone(schema);
}

/** Returns a cached validator for the exported schema `name`. */
export function schemaValidator(name: SchemaName): ValidateFunction {
  let validate = compiled.get(name);
  if (validate === undefined) {
    shared ??= createAjv();
    validate = shared.compile(loadSchema(name));
    compiled.set(name, validate);
  }
  return validate;
}

/** Converts Ajv errors into {@link SchemaError} values (prefixing paths), sorted by path. */
export function toSchemaErrors(errors: readonly ErrorObject[] | null | undefined, prefix = ""): SchemaError[] {
  const out = (errors ?? []).map((error) => {
    let message = error.message ?? error.keyword;
    if (error.keyword === "additionalProperties" && typeof error.params["additionalProperty"] === "string") {
      message += ` '${error.params["additionalProperty"]}'`;
    }
    if (message.length > MAX_MESSAGE) message = `${message.slice(0, MAX_MESSAGE - 1)}…`;
    return { path: prefix + error.instancePath, message };
  });
  const unique = new Map(out.map((e) => [`${e.path}\u0000${e.message}`, e]));
  return [...unique.values()].sort((a, b) => (a.path === b.path ? a.message.localeCompare(b.message)
    : a.path < b.path ? -1 : 1));
}

/** Validates `document` against the exported schema `name`; returns every problem (empty when valid). */
export function shapeErrors(name: SchemaName, document: unknown): SchemaError[] {
  const validate = schemaValidator(name);
  return validate(document) ? [] : toSchemaErrors(validate.errors);
}
