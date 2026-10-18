"""Tests for scripts/sdk/preprocess-dart.py (prompt 141): OpenAPI 3.1 -> 3.0.3 for the dart-dio generator.

Run with ``python3 -m pytest scripts/sdk/tests`` (needs pytest; the whole-spec validation also needs
openapi-spec-validator, pinned in sdk/versions.properties, and is skipped without it).
"""
from __future__ import annotations

import copy
import importlib.util
import json
import pathlib
import unittest

import pytest

ROOT = pathlib.Path(__file__).resolve().parents[3]
SCRIPT = ROOT / "scripts" / "sdk" / "preprocess-dart.py"
SPEC = ROOT / "docs" / "OpenAPI.json"


def load_module():
    """Loads preprocess-dart.py (its file name is not an importable module name)."""
    spec = importlib.util.spec_from_file_location("preprocess_dart", SCRIPT)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


pp = load_module()


def doc(schemas=None, **extra):
    """A minimal OpenAPI 3.1 document holding ``schemas``."""
    d = {
        "openapi": "3.1.0",
        "info": {"title": "T", "version": "1.0.0"},
        "paths": {},
        "components": {"schemas": schemas or {}},
    }
    d.update(extra)
    return d


def schema(s):
    """Converts one schema through a whole document and returns it."""
    return pp.preprocess(doc({"S": s}))["components"]["schemas"]["S"]


class PreprocessDartSpecTest(unittest.TestCase):
    """Each transformation of the preprocess step, plus the whole authored spec."""

    # --- nullable types ---------------------------------------------------------------------------

    def test_type_null_list_becomes_type_and_nullable(self):
        self.assertEqual(schema({"type": ["string", "null"], "format": "date-time"}),
                         {"type": "string", "format": "date-time", "nullable": True})

    def test_type_list_without_null_and_single_type_is_unwrapped(self):
        self.assertEqual(schema({"type": ["integer"]}), {"type": "integer"})

    def test_several_non_null_types_drop_type_and_keep_original(self):
        out = schema({"type": ["string", "number", "null"]})
        self.assertNotIn("type", out)
        self.assertTrue(out["nullable"])
        self.assertEqual(out["x-foresignal-oas31-type"], ["string", "number", "null"])

    def test_bare_null_type_becomes_nullable(self):
        self.assertEqual(schema({"type": "null"}), {"nullable": True})

    # --- nullable unions --------------------------------------------------------------------------

    def test_one_of_ref_and_null_becomes_all_of_and_nullable(self):
        out = schema({"oneOf": [{"$ref": "#/components/schemas/Geometry"}, {"type": "null"}]})
        self.assertEqual(out, {"allOf": [{"$ref": "#/components/schemas/Geometry"}], "nullable": True})

    def test_any_of_ref_and_null_becomes_all_of_and_nullable(self):
        out = schema({"anyOf": [{"type": "null"}, {"$ref": "#/components/schemas/Tlp"}], "description": "d"})
        self.assertEqual(out, {"description": "d", "allOf": [{"$ref": "#/components/schemas/Tlp"}],
                               "nullable": True})

    def test_one_of_inline_and_null_is_merged(self):
        out = schema({"oneOf": [{"type": "object", "properties": {"state": {"type": "string"}}},
                                {"type": "null"}], "description": "Playback"})
        self.assertEqual(out, {"description": "Playback", "type": "object",
                               "properties": {"state": {"type": "string"}}, "nullable": True})

    def test_one_of_inline_with_clashing_keys_uses_all_of(self):
        out = schema({"description": "outer", "oneOf": [{"type": "string", "description": "inner"},
                                                        {"type": "null"}]})
        self.assertEqual(out["allOf"], [{"type": "string", "description": "inner"}])
        self.assertTrue(out["nullable"])

    def test_one_of_several_refs_and_null_keeps_union_without_null(self):
        out = schema({"oneOf": [{"$ref": "#/components/schemas/A"}, {"$ref": "#/components/schemas/B"},
                                {"type": "null"}]})
        self.assertEqual(out["oneOf"], [{"$ref": "#/components/schemas/A"}, {"$ref": "#/components/schemas/B"}])
        self.assertTrue(out["nullable"])

    def test_nested_nullable_inside_properties_items_and_additional_properties(self):
        out = schema({
            "type": "object",
            "properties": {"a": {"type": ["number", "null"]}},
            "additionalProperties": {"type": ["string", "null"]},
            "items": {"oneOf": [{"$ref": "#/components/schemas/X"}, {"type": "null"}]},
        })
        self.assertEqual(out["properties"]["a"], {"type": "number", "nullable": True})
        self.assertEqual(out["additionalProperties"], {"type": "string", "nullable": True})
        self.assertEqual(out["items"], {"allOf": [{"$ref": "#/components/schemas/X"}], "nullable": True})

    # --- const, enum, examples, bounds -----------------------------------------------------------

    def test_const_becomes_single_value_enum(self):
        self.assertEqual(schema({"type": "integer", "const": 1}), {"type": "integer", "enum": [1]})

    def test_enum_with_null_drops_null_and_sets_nullable(self):
        out = schema({"type": ["string", "null"], "enum": ["read", "agent", None]})
        self.assertEqual(out, {"type": "string", "enum": ["read", "agent"], "nullable": True})

    def test_schema_examples_list_becomes_example(self):
        self.assertEqual(schema({"type": "string", "examples": ["a", "b"]}), {"type": "string", "example": "a"})

    def test_empty_examples_list_is_dropped(self):
        self.assertEqual(schema({"type": "string", "examples": []}), {"type": "string"})

    def test_examples_map_on_media_type_is_kept(self):
        examples = {"same": {"summary": "s", "value": "EAS: ZCZC"}}
        d = doc(paths={"/p": {"post": {"requestBody": {"content": {"text/plain": {
            "schema": {"type": "string"}, "examples": examples}}}, "responses": {"204": {"description": "ok"}}}}})
        out = pp.preprocess(d)
        self.assertEqual(out["paths"]["/p"]["post"]["requestBody"]["content"]["text/plain"]["examples"], examples)

    def test_numeric_exclusive_minimum_becomes_boolean_form(self):
        self.assertEqual(schema({"type": "number", "exclusiveMinimum": 0}),
                         {"type": "number", "minimum": 0, "exclusiveMinimum": True})

    def test_numeric_exclusive_maximum_keeps_stricter_bound(self):
        self.assertEqual(schema({"type": "number", "maximum": 10, "exclusiveMaximum": 5}),
                         {"type": "number", "maximum": 5, "exclusiveMaximum": True})
        self.assertEqual(schema({"type": "number", "minimum": 3, "exclusiveMinimum": 1}),
                         {"type": "number", "minimum": 3})

    def test_array_without_items_gains_empty_items(self):
        self.assertEqual(schema({"type": "array", "maxItems": 0}), {"type": "array", "maxItems": 0, "items": {}})

    def test_ref_with_siblings_becomes_all_of(self):
        out = schema({"type": "object", "properties": {
            "g": {"$ref": "#/components/schemas/Geometry", "description": "where"}}})
        self.assertEqual(out["properties"]["g"], {"description": "where",
                                                  "allOf": [{"$ref": "#/components/schemas/Geometry"}]})

    def test_plain_ref_is_untouched(self):
        self.assertEqual(schema({"$ref": "#/components/schemas/Geometry"}), {"$ref": "#/components/schemas/Geometry"})

    # --- dropped keywords and document members ----------------------------------------------------

    def test_json_schema_only_keywords_are_dropped(self):
        out = schema({"type": "object", "propertyNames": {"pattern": "^[a-z]+$"},
                      "if": {"required": ["a"]}, "then": {"required": ["b"]}, "$comment": "c",
                      "$schema": "https://json-schema.org/draft/2020-12/schema"})
        self.assertEqual(out, {"type": "object"})

    def test_content_encoding_base64_becomes_format_byte(self):
        self.assertEqual(schema({"type": "string", "contentEncoding": "base64", "contentMediaType": "image/png"}),
                         {"type": "string", "format": "byte"})

    def test_document_members_dropped_and_version_set(self):
        d = doc(webhooks={"w": {"post": {"responses": {"200": {"description": "ok"}}}}},
                jsonSchemaDialect="https://spec.openapis.org/oas/3.1/dialect/base")
        d["$schema"] = "https://spec.openapis.org/oas/3.1/schema/2022-10-07"
        d["info"]["license"] = {"name": "MIT", "identifier": "MIT"}
        d["info"]["summary"] = "s"
        out = pp.preprocess(d)
        self.assertEqual(out["openapi"], "3.0.3")
        for key in ("webhooks", "jsonSchemaDialect", "$schema"):
            self.assertNotIn(key, out)
        self.assertEqual(out["info"]["license"], {"name": "MIT", "url": "https://opensource.org/licenses/MIT"})
        self.assertNotIn("summary", out["info"])

    # --- what must survive ------------------------------------------------------------------------

    def test_x_foresignal_extensions_are_kept_everywhere(self):
        d = doc({"S": {"type": ["string", "null"], "x-foresignal-prompt": "141"}},
                paths={"/v1/x": {"get": {"operationId": "x", "x-foresignal-scope": "read",
                                         "x-foresignal-status": "implemented",
                                         "responses": {"200": {"description": "ok"}}}}})
        d["info"]["x-foresignal-changelog"] = [{"version": "1.0.0"}]
        out = pp.preprocess(d)
        self.assertEqual(out["info"]["x-foresignal-changelog"], [{"version": "1.0.0"}])
        self.assertEqual(out["paths"]["/v1/x"]["get"]["x-foresignal-scope"], "read")
        self.assertEqual(out["paths"]["/v1/x"]["get"]["x-foresignal-status"], "implemented")
        self.assertEqual(out["components"]["schemas"]["S"]["x-foresignal-prompt"], "141")

    def test_extension_contents_are_not_transformed(self):
        ext = {"hello": {"schema": {"type": ["string", "null"], "const": 1}}}
        d = doc(paths={"/s": {"get": {"responses": {"200": {"description": "ok", "x-foresignal-sse-events": ext}}}}})
        out = pp.preprocess(d)
        self.assertEqual(out["paths"]["/s"]["get"]["responses"]["200"]["x-foresignal-sse-events"], ext)

    def test_property_named_like_a_keyword_is_a_property(self):
        out = schema({"type": "object", "properties": {
            "const": {"type": "array", "items": {"type": "number"}},
            "type": {"type": "string"},
            "examples": {"type": "object"}}})
        self.assertEqual(out["properties"]["const"], {"type": "array", "items": {"type": "number"}})
        self.assertEqual(out["properties"]["type"], {"type": "string"})
        self.assertEqual(out["properties"]["examples"], {"type": "object"})

    def test_example_and_default_values_are_opaque(self):
        out = schema({"type": "object", "example": {"type": ["a", "null"], "const": 1},
                      "properties": {"n": {"type": "integer", "default": 3}}})
        self.assertEqual(out["example"], {"type": ["a", "null"], "const": 1})
        self.assertEqual(out["properties"]["n"], {"type": "integer", "default": 3})

    def test_parameter_and_header_schemas_are_converted(self):
        d = doc(paths={"/p": {"get": {
            "parameters": [{"name": "after", "in": "query", "schema": {"type": ["integer", "null"]}}],
            "responses": {"200": {"description": "ok", "headers": {
                "Retry-After": {"schema": {"type": "integer", "exclusiveMinimum": 0}}}}}}}})
        out = pp.preprocess(d)
        op = out["paths"]["/p"]["get"]
        self.assertEqual(op["parameters"][0]["schema"], {"type": "integer", "nullable": True})
        self.assertEqual(op["responses"]["200"]["headers"]["Retry-After"]["schema"],
                         {"type": "integer", "minimum": 0, "exclusiveMinimum": True})

    def test_input_document_is_not_modified(self):
        d = doc({"S": {"type": ["string", "null"], "const": "x"}}, webhooks={"w": {}})
        before = copy.deepcopy(d)
        pp.preprocess(d)
        self.assertEqual(d, before)

    # --- dart-dio workarounds ---------------------------------------------------------------------

    def test_constraint_only_any_of_is_moved_to_extension(self):
        branches = [{"required": ["push"]}, {"required": ["pull"]}]
        out = schema({"type": "object", "properties": {"push": {"type": "boolean"}}, "anyOf": branches})
        self.assertNotIn("anyOf", out)
        self.assertEqual(out["x-foresignal-oas31-anyOf"], branches)

    def test_union_of_primitives_becomes_untyped(self):
        out = schema({"oneOf": [{"type": "number"}, {"type": "string"}, {"type": "boolean"}]})
        self.assertNotIn("oneOf", out)
        self.assertEqual(len(out["x-foresignal-oas31-oneOf"]), 3)

    def test_union_of_models_is_kept(self):
        out = schema({"oneOf": [{"$ref": "#/components/schemas/A"},
                                {"type": "object", "properties": {"b": {"type": "string"}}}]})
        self.assertEqual(len(out["oneOf"]), 2)

    def test_enum_with_apostrophe_is_moved_to_extension(self):
        out = schema({"type": "string", "enum": ["hPa", "[in_i'Hg]"]})
        self.assertNotIn("enum", out)
        self.assertEqual(out["x-foresignal-oas31-enum"], ["hPa", "[in_i'Hg]"])

    def test_enum_and_array_defaults_are_moved_to_extension(self):
        self.assertEqual(schema({"type": "string", "enum": ["a", "b"], "default": "a"}),
                         {"type": "string", "enum": ["a", "b"], "x-foresignal-oas31-default": "a"})
        out = schema({"type": "array", "items": {"type": "string"}, "default": ["x"]})
        self.assertEqual(out["x-foresignal-oas31-default"], ["x"])
        self.assertNotIn("default", out)

    # --- the authored spec ------------------------------------------------------------------------

    def test_authored_spec_has_no_31_constructs_left(self):
        out = pp.preprocess(json.loads(SPEC.read_text(encoding="utf-8")))
        self.assertEqual(out["openapi"], "3.0.3")
        self.assertNotIn("webhooks", out)
        problems = []

        def walk(node, path, in_schema_data=False):
            if isinstance(node, dict):
                for k, v in node.items():
                    if k.startswith("x-") or k in ("example", "examples", "default", "enum"):
                        continue
                    if k == "type" and (isinstance(v, list) or v == "null"):
                        problems.append("/".join(path + [k]))
                    if k in ("exclusiveMinimum", "exclusiveMaximum") and not isinstance(v, bool):
                        problems.append("/".join(path + [k]))
                    walk(v, path + [str(k)])
            elif isinstance(node, list):
                for i, v in enumerate(node):
                    walk(v, path + [str(i)])

        walk(out, [])
        self.assertEqual(problems, [])

    def test_authored_spec_keeps_x_foresignal_on_every_operation(self):
        source = json.loads(SPEC.read_text(encoding="utf-8"))
        out = pp.preprocess(source)
        for path, item in source["paths"].items():
            for method, op in item.items():
                if isinstance(op, dict) and "operationId" in op:
                    converted = out["paths"][path][method]
                    for key in [k for k in op if k.startswith("x-foresignal-")]:
                        self.assertEqual(converted[key], op[key], f"{method} {path} {key}")

    def test_authored_spec_parses_as_openapi_303(self):
        validator = pytest.importorskip("openapi_spec_validator")
        out = pp.preprocess(json.loads(SPEC.read_text(encoding="utf-8")))
        validator.validate(out, cls=validator.OpenAPIV30SpecValidator)


def test_cli_writes_output(tmp_path):
    """The command line reads --input and writes an OpenAPI 3.0.3 file to --output."""
    source = tmp_path / "in.json"
    target = tmp_path / "nested" / "out.json"
    source.write_text(json.dumps(doc({"S": {"type": ["string", "null"]}})), encoding="utf-8")
    assert pp.main(["--input", str(source), "--output", str(target)]) == 0
    written = json.loads(target.read_text(encoding="utf-8"))
    assert written["openapi"] == "3.0.3"
    assert written["components"]["schemas"]["S"] == {"type": "string", "nullable": True}
