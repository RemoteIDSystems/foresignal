from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.mcp_auth_settings_scope_map_additional_property import (
    McpAuthSettingsScopeMapAdditionalProperty,
)

T = TypeVar("T", bound="McpAuthSettingsScopeMap")


@_attrs_define
class McpAuthSettingsScopeMap:
    """IdP scope → Foresignal scope; `admin` is never mappable

    Example:
        {'foresignal:read': 'read', 'foresignal:agent': 'agent'}

    """

    additional_properties: dict[str, McpAuthSettingsScopeMapAdditionalProperty] = (
        _attrs_field(init=False, factory=dict)
    )

    def to_dict(self) -> dict[str, Any]:

        field_dict: dict[str, Any] = {}
        for prop_name, prop in self.additional_properties.items():
            field_dict[prop_name] = prop.value

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        mcp_auth_settings_scope_map = cls()

        additional_properties = {}
        for prop_name, prop_dict in d.items():
            additional_property = McpAuthSettingsScopeMapAdditionalProperty(prop_dict)

            additional_properties[prop_name] = additional_property

        mcp_auth_settings_scope_map.additional_properties = additional_properties
        return mcp_auth_settings_scope_map

    @property
    def additional_keys(self) -> list[str]:
        return list(self.additional_properties.keys())

    def __getitem__(self, key: str) -> McpAuthSettingsScopeMapAdditionalProperty:
        return self.additional_properties[key]

    def __setitem__(
        self, key: str, value: McpAuthSettingsScopeMapAdditionalProperty
    ) -> None:
        self.additional_properties[key] = value

    def __delitem__(self, key: str) -> None:
        del self.additional_properties[key]

    def __contains__(self, key: str) -> bool:
        return key in self.additional_properties
