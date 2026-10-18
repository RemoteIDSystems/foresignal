from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import (
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.plugin_state import PluginState
from ..types import UNSET, Unset

T = TypeVar("T", bound="Plugin")


@_attrs_define
class Plugin:
    """An out-of-tree WASM parser plugin found under `${FORESIGNAL_DATA_DIR}/plugins/{name}/`

    Attributes:
        name (str):
        version (str):
        abi (Literal[1]): Always 1, the only ABI this host runs; a manifest declaring another is in state `error`
        kind (Literal['parser']): Always `parser`, the only kind of ABI v1; a manifest declaring another is in state
            `error`
        sha256 (str): Declared in plugin.json; the file's own hash when the manifest is invalid (empty when neither is
            known)
        hash_matches (bool): True when the declared and actual hashes are equal
        enabled (bool): True when the admin enabled this plugin (an approval bound to a SHA-256 is stored)
        state (PluginState): `error`: the manifest or module is invalid; `hash-mismatch`: the file does not hash to the
            declared SHA-256, or to the approved one; `enabled`: enabled, plugins on, hashes match and the module loads;
            `disabled`: otherwise
        actual_sha_256 (str | Unset): The SHA-256 of plugin.wasm as read now; absent when the file is missing
        error (None | str | Unset): Why the plugin will not load or run, or null
        source_ids (list[str] | Unset): `wasm-feed` sources using this plugin
        loaded_at (datetime.datetime | None | Unset): When its current module was last loaded to run (since this process
            started), or null
    """

    name: str
    version: str
    abi: Literal[1]
    kind: Literal["parser"]
    sha256: str
    hash_matches: bool
    enabled: bool
    state: PluginState
    actual_sha_256: str | Unset = UNSET
    error: None | str | Unset = UNSET
    source_ids: list[str] | Unset = UNSET
    loaded_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        version = self.version

        abi = self.abi

        kind = self.kind

        sha256 = self.sha256

        hash_matches = self.hash_matches

        enabled = self.enabled

        state = self.state.value

        actual_sha_256 = self.actual_sha_256

        error: None | str | Unset
        if isinstance(self.error, Unset):
            error = UNSET
        else:
            error = self.error

        source_ids: list[str] | Unset = UNSET
        if not isinstance(self.source_ids, Unset):
            source_ids = self.source_ids

        loaded_at: None | str | Unset
        if isinstance(self.loaded_at, Unset):
            loaded_at = UNSET
        elif isinstance(self.loaded_at, datetime.datetime):
            loaded_at = self.loaded_at.isoformat()
        else:
            loaded_at = self.loaded_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "version": version,
                "abi": abi,
                "kind": kind,
                "sha256": sha256,
                "hashMatches": hash_matches,
                "enabled": enabled,
                "state": state,
            }
        )
        if actual_sha_256 is not UNSET:
            field_dict["actualSha256"] = actual_sha_256
        if error is not UNSET:
            field_dict["error"] = error
        if source_ids is not UNSET:
            field_dict["sourceIds"] = source_ids
        if loaded_at is not UNSET:
            field_dict["loadedAt"] = loaded_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        name = d.pop("name")

        version = d.pop("version")

        abi = cast(Literal[1], d.pop("abi"))
        if abi != 1:
            raise ValueError(f"abi must match const 1, got '{abi}'")

        kind = cast(Literal["parser"], d.pop("kind"))
        if kind != "parser":
            raise ValueError(f"kind must match const 'parser', got '{kind}'")

        sha256 = d.pop("sha256")

        hash_matches = d.pop("hashMatches")

        enabled = d.pop("enabled")

        state = PluginState(d.pop("state"))

        actual_sha_256 = d.pop("actualSha256", UNSET)

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        source_ids = cast(list[str], d.pop("sourceIds", UNSET))

        def _parse_loaded_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                loaded_at_type_0 = datetime.datetime.fromisoformat(data)

                return loaded_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        loaded_at = _parse_loaded_at(d.pop("loadedAt", UNSET))

        plugin = cls(
            name=name,
            version=version,
            abi=abi,
            kind=kind,
            sha256=sha256,
            hash_matches=hash_matches,
            enabled=enabled,
            state=state,
            actual_sha_256=actual_sha_256,
            error=error,
            source_ids=source_ids,
            loaded_at=loaded_at,
        )

        plugin.additional_properties = d
        return plugin

    @property
    def additional_keys(self) -> list[str]:
        return list(self.additional_properties.keys())

    def __getitem__(self, key: str) -> Any:
        return self.additional_properties[key]

    def __setitem__(self, key: str, value: Any) -> None:
        self.additional_properties[key] = value

    def __delitem__(self, key: str) -> None:
        del self.additional_properties[key]

    def __contains__(self, key: str) -> bool:
        return key in self.additional_properties
