from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SystemInfoMcpTransports")


@_attrs_define
class SystemInfoMcpTransports:
    """Registered transports

    Attributes:
        streamable (bool | Unset):
        legacy_sse (bool | Unset): `foresignal.mcp.legacy-sse.enabled`: /mcp/sse + /mcp/message
    """

    streamable: bool | Unset = UNSET
    legacy_sse: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        streamable = self.streamable

        legacy_sse = self.legacy_sse

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if streamable is not UNSET:
            field_dict["streamable"] = streamable
        if legacy_sse is not UNSET:
            field_dict["legacySse"] = legacy_sse

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        streamable = d.pop("streamable", UNSET)

        legacy_sse = d.pop("legacySse", UNSET)

        system_info_mcp_transports = cls(
            streamable=streamable,
            legacy_sse=legacy_sse,
        )

        system_info_mcp_transports.additional_properties = d
        return system_info_mcp_transports

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
