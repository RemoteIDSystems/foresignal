from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="SinkCreateConfig")


@_attrs_define
class SinkCreateConfig:
    """Per-type settings validated against SinkType.configSchema. Common keys: `mode` (PUSH|SERVE where the type supports
    both), `bind` (default 127.0.0.1; loopback, RFC 1918, link-local, ULA, or 100.64.0.0/10 with `allowTailnet`), `port`
    (required for SERVE, no default), `allowAllInterfaces`, `maxClients` (default 8), `maxPerSecond`; broker and target
    URLs pass the outbound URL policy (035).

    """

    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        sink_create_config = cls()

        sink_create_config.additional_properties = d
        return sink_create_config

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
