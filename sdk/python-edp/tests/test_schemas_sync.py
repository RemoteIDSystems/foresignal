"""Guard: the packaged schemas equal ``docs/protocol/edp-1.0`` (refresh with ``scripts/sync_schemas.py``)."""

from __future__ import annotations

from importlib import resources

import pytest

from foresignal_edp.protocol import SCHEMA_NAMES

from .conftest import EXPORTED_SCHEMAS_DIR


def test_every_schema_is_packaged() -> None:
    packaged = resources.files("foresignal_edp").joinpath("schemas")
    assert sorted(p.name for p in packaged.iterdir() if p.name.endswith(".schema.json")) == sorted(
        f"{name}.schema.json" for name in SCHEMA_NAMES)


@pytest.mark.parametrize("name", SCHEMA_NAMES)
def test_packaged_schema_matches_the_exported_one(name: str) -> None:
    exported = EXPORTED_SCHEMAS_DIR / f"{name}.schema.json"
    if not exported.is_file():
        pytest.skip(f"{exported} not available outside the repository")
    packaged = resources.files("foresignal_edp").joinpath("schemas").joinpath(f"{name}.schema.json").read_bytes()
    assert packaged == exported.read_bytes(), f"run scripts/sync_schemas.py: {name}.schema.json differs"
