from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.system_info_mcp_transports import SystemInfoMcpTransports


T = TypeVar("T", bound="SystemInfoMcp")


@_attrs_define
class SystemInfoMcp:
    """MCP server state (025)

    Attributes:
        enabled (bool | Unset):
        sessions (int | Unset):
        transports (SystemInfoMcpTransports | Unset): Registered transports
        tools (int | Unset): Registered tools
    """

    enabled: bool | Unset = UNSET
    sessions: int | Unset = UNSET
    transports: SystemInfoMcpTransports | Unset = UNSET
    tools: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        enabled = self.enabled

        sessions = self.sessions

        transports: dict[str, Any] | Unset = UNSET
        if not isinstance(self.transports, Unset):
            transports = self.transports.to_dict()

        tools = self.tools

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if enabled is not UNSET:
            field_dict["enabled"] = enabled
        if sessions is not UNSET:
            field_dict["sessions"] = sessions
        if transports is not UNSET:
            field_dict["transports"] = transports
        if tools is not UNSET:
            field_dict["tools"] = tools

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.system_info_mcp_transports import SystemInfoMcpTransports

        d = dict(src_dict)
        enabled = d.pop("enabled", UNSET)

        sessions = d.pop("sessions", UNSET)

        _transports = d.pop("transports", UNSET)
        transports: SystemInfoMcpTransports | Unset
        if isinstance(_transports, Unset):
            transports = UNSET
        else:
            transports = SystemInfoMcpTransports.from_dict(_transports)

        tools = d.pop("tools", UNSET)

        system_info_mcp = cls(
            enabled=enabled,
            sessions=sessions,
            transports=transports,
            tools=tools,
        )

        system_info_mcp.additional_properties = d
        return system_info_mcp

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
