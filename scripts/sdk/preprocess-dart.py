#!/usr/bin/env python3
"""Turn docs/OpenAPI.json (OpenAPI 3.1) into an OpenAPI 3.0.3 copy for the dart-dio generator.

openapi-generator's ``dart-dio`` generator handles OpenAPI 3.1 nullable unions badly (prompt 141,
docs/Gap-Analysis.md section 2), so the Dart SDK is generated from a pre-processed copy. The copy is a
build artifact; the authored spec is never changed.

Transformations (schema objects only; examples, defaults, enum values and ``x-*`` extensions are
opaque data and are copied unchanged):

- ``type: [T, "null"]`` becomes ``type: T`` plus ``nullable: true``. A list with several non-null
  types cannot be expressed in 3.0: ``type`` is dropped (any value) and the original list is kept
  as ``x-foresignal-oas31-type``.
- ``oneOf``/``anyOf`` with a ``{type: "null"}`` branch: the null branch is removed and
  ``nullable: true`` is set. A single remaining ``$ref`` becomes ``allOf: [{$ref}]``; a single
  remaining inline schema is merged into the parent when no key clashes (else ``allOf``); several
  remaining branches stay a ``oneOf``/``anyOf``.
- ``const`` becomes a single-value ``enum``; a ``null`` inside ``enum`` is removed and becomes
  ``nullable: true``.
- ``examples`` (a JSON Schema list) becomes ``example`` (its first element). ``examples`` maps of
  Example Objects on parameters and media types are valid 3.0 and kept.
- Numeric ``exclusiveMinimum``/``exclusiveMaximum`` become ``minimum``/``maximum`` plus the 3.0
  boolean form.
- ``type: array`` without ``items`` gains ``items: {}`` (``items`` is required in 3.0).
- A schema ``$ref`` with sibling keywords becomes ``allOf: [{$ref}]`` plus the siblings (3.0 ignores
  siblings of a Reference Object).
- 3.1-only keywords with no 3.0 equivalent are dropped: ``propertyNames``, ``if``/``then``/``else``,
  ``contentMediaType`` (``contentEncoding: base64`` becomes ``format: byte``), ``$id``, ``$anchor``,
  ``$comment``, ``prefixItems``, ``unevaluatedProperties``, ``dependentRequired``,
  ``dependentSchemas``, ``contains``, ``minContains``, ``maxContains``.
- ``webhooks``, ``jsonSchemaDialect`` and every ``$schema`` are dropped, ``info.license.identifier``
  (3.1 only) is dropped, and ``openapi`` is set to ``3.0.3``.
- ``x-foresignal-*`` extensions are kept everywhere.
- dart-dio workarounds (see ``apply_dart_workarounds``): constraint-only unions, unions of
  primitives and enums with a ``'`` in a value are moved to ``x-foresignal-oas31-*`` extensions.

Usage::

    python3 scripts/sdk/preprocess-dart.py [--input docs/OpenAPI.json] [--output build/sdk/openapi-dart.json]
"""
from __future__ import annotations

import argparse
import copy
import json
import pathlib
import sys
from typing import Any

#: Keys of a schema whose value is a map of name -> schema.
SCHEMA_MAP_KEYS = ("properties", "patternProperties", "$defs", "definitions")
#: Keys of a schema whose value is a single sub-schema.
SCHEMA_SINGLE_KEYS = ("items", "additionalProperties", "not")
#: Keys of a schema whose value is a list of sub-schemas.
SCHEMA_LIST_KEYS = ("allOf", "anyOf", "oneOf")
#: Keys of a schema whose value is data, never a schema.
SCHEMA_OPAQUE_KEYS = ("example", "examples", "default", "enum", "const")
#: JSON Schema 2020-12 keywords with no OpenAPI 3.0.3 equivalent.
DROPPED_SCHEMA_KEYS = (
    "propertyNames", "if", "then", "else", "contentMediaType", "$id", "$anchor", "$comment",
    "prefixItems", "unevaluatedProperties", "unevaluatedItems", "dependentRequired",
    "dependentSchemas", "contains", "minContains", "maxContains", "$schema", "$dynamicRef",
    "$dynamicAnchor", "$vocabulary",
)
#: Keys of a non-schema object whose value is data, never walked.
OTHER_OPAQUE_KEYS = ("example", "default", "enum", "value")


def is_null_schema(node: Any) -> bool:
    """Returns whether ``node`` is the null branch of a union (description allowed).

    The branches are converted before the union is, so the 3.1 ``{type: "null"}`` arrives here as
    ``{nullable: true}``; both forms are the null branch.
    """
    if not isinstance(node, dict):
        return False
    if node.get("type") == "null":
        marker = "type"
    elif node.get("nullable") is True and "type" not in node:
        marker = "nullable"
    else:
        return False
    return all(k in (marker, "description", "title") or k.startswith("x-") for k in node)


def convert_schema(node: Any) -> Any:
    """Returns the OpenAPI 3.0.3 form of one schema object (recursively)."""
    if not isinstance(node, dict):
        return node
    out: dict[str, Any] = {}
    nullable = False
    for key, value in node.items():
        if key.startswith("x-"):
            out[key] = copy.deepcopy(value)
        elif key in SCHEMA_OPAQUE_KEYS:
            out[key] = copy.deepcopy(value)
        elif key in SCHEMA_MAP_KEYS and isinstance(value, dict):
            out[key] = {name: convert_schema(sub) for name, sub in value.items()}
        elif key in SCHEMA_SINGLE_KEYS and isinstance(value, dict):
            out[key] = convert_schema(value)
        elif key in SCHEMA_LIST_KEYS and isinstance(value, list):
            out[key] = [convert_schema(sub) for sub in value]
        elif key in DROPPED_SCHEMA_KEYS:
            continue
        else:
            out[key] = copy.deepcopy(value)

    # $schema / 3.1-only keywords are gone; contentEncoding base64 becomes format byte.
    encoding = out.pop("contentEncoding", None)
    if encoding in ("base64", "base64url") and "format" not in out:
        out["format"] = "byte"

    # type: [T, "null"]
    t = out.get("type")
    if isinstance(t, list):
        non_null = [x for x in t if x != "null"]
        if len(non_null) < len(t):
            nullable = True
        if len(non_null) == 1:
            out["type"] = non_null[0]
        elif not non_null:
            del out["type"]
        else:
            del out["type"]
            out["x-foresignal-oas31-type"] = list(t)
    elif t == "null":
        del out["type"]
        nullable = True

    # 3.0 requires items on arrays; 3.1 does not.
    if out.get("type") == "array" and "items" not in out:
        out["items"] = {}

    # const -> single-value enum
    if "const" in out:
        out["enum"] = [out.pop("const")]

    # enum with null -> nullable
    if isinstance(out.get("enum"), list) and None in out["enum"]:
        nullable = True
        out["enum"] = [v for v in out["enum"] if v is not None]
        if not out["enum"]:
            del out["enum"]

    # examples (list) -> example
    examples = out.get("examples")
    if isinstance(examples, list):
        del out["examples"]
        if examples and "example" not in out:
            out["example"] = examples[0]

    # numeric exclusive bounds -> 3.0 boolean form
    for exclusive, bound in (("exclusiveMinimum", "minimum"), ("exclusiveMaximum", "maximum")):
        value = out.get(exclusive)
        if isinstance(value, (int, float)) and not isinstance(value, bool):
            if bound in out:
                # Keep the stricter of the two bounds.
                stricter = max(out[bound], value) if bound == "minimum" else min(out[bound], value)
                exclusive_wins = stricter == value
                out[bound] = stricter
                out[exclusive] = exclusive_wins
                if not exclusive_wins:
                    del out[exclusive]
            else:
                out[bound] = value
                out[exclusive] = True

    # oneOf / anyOf with a null branch
    for union in ("oneOf", "anyOf"):
        branches = out.get(union)
        if not isinstance(branches, list):
            continue
        rest = [b for b in branches if not is_null_schema(b)]
        if len(rest) == len(branches):
            continue
        nullable = True
        del out[union]
        if len(rest) == 1:
            only = rest[0]
            if isinstance(only, dict) and "$ref" in only:
                out["allOf"] = [only]
            elif isinstance(only, dict) and not (set(only) & set(out)):
                out.update(only)
            else:
                out["allOf"] = [only]
        elif rest:
            out[union] = rest

    apply_dart_workarounds(out)

    # $ref with siblings -> allOf
    if "$ref" in out and len(out) > 1:
        ref = out.pop("$ref")
        all_of = out.get("allOf", [])
        out["allOf"] = [{"$ref": ref}] + list(all_of)

    if nullable:
        out["nullable"] = True
    return out


def is_modelable_branch(branch: Any) -> bool:
    """Returns whether a union branch maps to a Dart class: a ``$ref``, an ``allOf`` or an object with properties."""
    if not isinstance(branch, dict):
        return False
    if "$ref" in branch or "allOf" in branch:
        return True
    return branch.get("type") == "object" and "properties" in branch


def apply_dart_workarounds(out: dict[str, Any]) -> None:
    """Rewrites, in place, the constructs that make dart-dio (json_serializable) emit code that does not compile.

    - A ``oneOf``/``anyOf`` on a schema that also has ``properties`` is a validation constraint (for
      example "push or pull is required"); it is dropped and kept as ``x-foresignal-oas31-<keyword>``.
    - A ``oneOf``/``anyOf`` none of whose branches is a ``$ref`` or an object model (unions of
      primitives and arrays) becomes an untyped schema (any JSON value), the union kept the same way.
    - A string ``enum`` with a value containing ``'`` is dropped (the generator emits an invalid raw
      string literal for it) and kept as ``x-foresignal-oas31-enum``.
    - A ``default`` on an enum, a referenced schema, an array or an object is dropped (the generator emits a call to a
      constructor that does not exist) and kept as ``x-foresignal-oas31-default``; the server applies
      its defaults anyway.
    """
    for union in ("oneOf", "anyOf"):
        branches = out.get(union)
        if not isinstance(branches, list):
            continue
        constraint_only = "properties" in out
        primitive_only = not any(is_modelable_branch(b) for b in branches)
        if constraint_only or primitive_only:
            out["x-foresignal-oas31-" + union] = out.pop(union)
    values = out.get("enum")
    if isinstance(values, list) and any(isinstance(v, str) and "'" in v for v in values):
        out["x-foresignal-oas31-enum"] = out.pop("enum")
    if "default" in out and (
            "enum" in out or "allOf" in out or "$ref" in out
            or isinstance(out["default"], (list, dict))
            or out.get("type") in ("array", "object")):
        out["x-foresignal-oas31-default"] = out.pop("default")


def convert_other(node: Any, key: str | None = None) -> Any:
    """Returns the 3.0.3 form of a non-schema object (paths, parameters, responses, components)."""
    if isinstance(node, list):
        return [convert_other(item) for item in node]
    if not isinstance(node, dict):
        return node
    out: dict[str, Any] = {}
    for k, v in node.items():
        if k.startswith("x-"):
            out[k] = copy.deepcopy(v)
        elif k in ("$schema", "jsonSchemaDialect", "webhooks"):
            continue
        elif k == "schema":
            out[k] = convert_schema(v)
        elif k == "schemas" and key == "components" and isinstance(v, dict):
            out[k] = {name: convert_schema(s) for name, s in v.items()}
        elif k in OTHER_OPAQUE_KEYS:
            out[k] = copy.deepcopy(v)
        elif k == "examples":
            # Parameter / media type examples: a map of Example Objects, valid in 3.0.
            out[k] = copy.deepcopy(v)
        else:
            out[k] = convert_other(v, k)
    return out


def preprocess(spec: dict[str, Any]) -> dict[str, Any]:
    """Returns an OpenAPI 3.0.3 copy of an OpenAPI 3.1 document (the input is not modified)."""
    out = convert_other(spec)
    out["openapi"] = "3.0.3"
    info = out.get("info")
    if isinstance(info, dict):
        lic = info.get("license")
        if isinstance(lic, dict):
            lic.pop("identifier", None)
            if "url" not in lic and spec.get("info", {}).get("license", {}).get("identifier") == "MIT":
                lic["url"] = "https://opensource.org/licenses/MIT"
        info.pop("summary", None)
    components = out.get("components")
    if isinstance(components, dict):
        components.pop("pathItems", None)
    return out


def main(argv: list[str] | None = None) -> int:
    """Command-line entry point: reads ``--input`` and writes ``--output``."""
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--input", default="docs/OpenAPI.json", help="OpenAPI 3.1 document")
    parser.add_argument("--output", default="build/sdk/openapi-dart.json", help="OpenAPI 3.0.3 copy to write")
    args = parser.parse_args(argv)
    source = pathlib.Path(args.input)
    with source.open(encoding="utf-8") as f:
        spec = json.load(f)
    result = preprocess(spec)
    target = pathlib.Path(args.output)
    target.parent.mkdir(parents=True, exist_ok=True)
    with target.open("w", encoding="utf-8") as f:
        json.dump(result, f, indent=2, ensure_ascii=False)
        f.write("\n")
    print(f"preprocess-dart: wrote {target} (OpenAPI 3.0.3, {len(result.get('paths', {}))} paths)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
