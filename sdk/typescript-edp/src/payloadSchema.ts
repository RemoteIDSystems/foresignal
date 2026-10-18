/**
 * Vetting and compiling a stream's `payloadSchema` (EDP §7): at most 64 KB serialized, only local
 * `$ref`s (starting with `#`), no `$dynamicRef` or `$recursiveRef`, no `$schema` other than 2020-12,
 * and every `pattern` and `patternProperties` key at most 256 characters and acceptable to a
 * linear-time engine. The server compiles patterns with RE2; this library refuses the constructs RE2
 * rejects (look-around, backreferences, atomic groups, possessive quantifiers) and then compiles them
 * as Ajv does (`new RegExp(pattern, "u")`). The server remains the final judge.
 */
import type { ValidateFunction } from "ajv/dist/2020.js";
import { PayloadSchemaError } from "./errors.ts";
import type { SchemaError } from "./errors.ts";
import { MAX_PATTERN_LENGTH, MAX_SCHEMA_BYTES } from "./protocol.ts";
import { createAjv, toSchemaErrors } from "./schemas.ts";

/** The only `$schema` a payload schema may declare. */
export const DIALECT = "https://json-schema.org/draft/2020-12/schema";

const NOT_LINEAR = /\(\?<?[=!]|\(\?>|\(\?P=|\\[1-9]|\\k<|[*+?}]\+/;
const encoder = new TextEncoder();

function escape(key: string): string {
  return key.replace(/~/g, "~0").replace(/\//g, "~1");
}

/** Adds a problem when `regex` is too long, uses a construct a linear-time engine refuses, or does not compile. */
export function checkPattern(regex: string, path: string, problems: SchemaError[]): void {
  if (regex.length > MAX_PATTERN_LENGTH) {
    problems.push({ path, message: `pattern is ${regex.length} characters; the maximum is ${MAX_PATTERN_LENGTH}` });
    return;
  }
  if (NOT_LINEAR.test(regex)) {
    problems.push({ path, message: "pattern is not supported by the linear-time engine (look-around, "
      + "backreferences, atomic groups and possessive quantifiers are refused)" });
    return;
  }
  try {
    new RegExp(regex, "u");
  } catch (e) {
    problems.push({ path, message: `pattern is not supported by the linear-time engine: ${(e as Error).message}` });
  }
}

function walk(node: unknown, path: string, problems: SchemaError[]): void {
  if (Array.isArray(node)) {
    node.forEach((child, i) => walk(child, `${path}/${i}`, problems));
    return;
  }
  if (node === null || typeof node !== "object") return;
  for (const [key, value] of Object.entries(node)) {
    const child = `${path}/${escape(key)}`;
    if (key === "$ref") {
      if (typeof value !== "string" || !value.startsWith("#")) {
        problems.push({ path: child, message: "only local references (starting with #) are allowed" });
      }
    } else if (key === "$dynamicRef" || key === "$recursiveRef") {
      problems.push({ path: child, message: `${key} is not allowed` });
    } else if (key === "pattern") {
      if (typeof value === "string") checkPattern(value, child, problems);
    } else if (key === "patternProperties" && value !== null && typeof value === "object" && !Array.isArray(value)) {
      for (const pattern of Object.keys(value)) checkPattern(pattern, `${child}/${escape(pattern)}`, problems);
    }
    walk(value, child, problems);
  }
}

/** Returns the §7 problems of `schema` without compiling it (empty when acceptable). */
export function vetPayloadSchema(schema: unknown): SchemaError[] {
  if (schema === null || typeof schema !== "object" || Array.isArray(schema)) {
    return [{ path: "", message: "must be a JSON object" }];
  }
  const size = encoder.encode(JSON.stringify(schema)).length;
  if (size > MAX_SCHEMA_BYTES) return [{ path: "", message: `is ${size} bytes; the maximum is ${MAX_SCHEMA_BYTES}` }];
  const problems: SchemaError[] = [];
  const dialect = (schema as Record<string, unknown>)["$schema"];
  if (dialect !== undefined && dialect !== null && dialect !== DIALECT) {
    problems.push({ path: "/$schema", message: `must be absent or ${DIALECT}` });
  }
  walk(schema, "", problems);
  return problems;
}

/** A compiled payload schema; validates an item's `properties`. */
export class PayloadValidator {
  /** The schema it was compiled from. */
  readonly schema: Record<string, unknown>;
  private readonly validateFn: ValidateFunction;

  /** Wraps a compiled validator. Use {@link compilePayloadSchema} instead. */
  constructor(schema: Record<string, unknown>, validateFn: ValidateFunction) {
    this.schema = schema;
    this.validateFn = validateFn;
  }

  /** Returns the problems of `properties` (`null`/`undefined` is checked as an empty object). */
  validate(properties: unknown): SchemaError[] {
    const value = properties === null || properties === undefined ? {} : properties;
    return this.validateFn(value) ? [] : toSchemaErrors(this.validateFn.errors);
  }
}

/**
 * Vets and compiles `schema`.
 *
 * @throws PayloadSchemaError with every problem found, paths relative to the schema root
 */
export function compilePayloadSchema(schema: unknown): PayloadValidator {
  const problems = vetPayloadSchema(schema);
  if (problems.length > 0) throw new PayloadSchemaError(problems);
  const record = structuredClone(schema) as Record<string, unknown>;
  const ajv = createAjv();
  if (!ajv.validateSchema(record)) {
    throw new PayloadSchemaError(toSchemaErrors(ajv.errors));
  }
  try {
    return new PayloadValidator(record, ajv.compile(record));
  } catch (e) {
    throw new PayloadSchemaError([{ path: "", message: `cannot be compiled: ${(e as Error).message}` }]);
  }
}
