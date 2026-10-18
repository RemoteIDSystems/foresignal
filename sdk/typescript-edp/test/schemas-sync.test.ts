/** Guard: the embedded schemas equal docs/protocol/edp-1.0 (refresh with `npm run sync-schemas`). */
import { existsSync, readFileSync } from "node:fs";
import { join } from "node:path";
import { describe, expect, it } from "vitest";
import { EXPORTED_SCHEMAS } from "../src/generated/schemas.ts";
import { SCHEMA_NAMES } from "../src/index.ts";
import { EXPORTED_SCHEMAS_DIR } from "./helpers.ts";

describe("embedded schemas", () => {
  it("embeds every exported schema", () => {
    expect(Object.keys(EXPORTED_SCHEMAS).sort()).toEqual([...SCHEMA_NAMES].sort());
  });

  it.each([...SCHEMA_NAMES])("%s matches docs/protocol/edp-1.0", (name) => {
    const path = join(EXPORTED_SCHEMAS_DIR, `${name}.schema.json`);
    if (!existsSync(path)) return; // outside the repository
    expect(EXPORTED_SCHEMAS[name], `run npm run sync-schemas: ${name}.schema.json differs`)
      .toEqual(JSON.parse(readFileSync(path, "utf8")));
  });
});
