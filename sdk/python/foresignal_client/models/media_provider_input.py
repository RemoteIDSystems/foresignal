from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="MediaProviderInput")


@_attrs_define
class MediaProviderInput:
    """Update a rating provider (admin); enabling requires licence acknowledgement

    Attributes:
        enabled (bool | Unset):
        licence_acknowledged (bool | Unset):
        permission_recorded (bool | Unset):
        api_key (str | Unset): MBFC API key (write-only)
    """

    enabled: bool | Unset = UNSET
    licence_acknowledged: bool | Unset = UNSET
    permission_recorded: bool | Unset = UNSET
    api_key: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        enabled = self.enabled

        licence_acknowledged = self.licence_acknowledged

        permission_recorded = self.permission_recorded

        api_key = self.api_key

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if enabled is not UNSET:
            field_dict["enabled"] = enabled
        if licence_acknowledged is not UNSET:
            field_dict["licenceAcknowledged"] = licence_acknowledged
        if permission_recorded is not UNSET:
            field_dict["permissionRecorded"] = permission_recorded
        if api_key is not UNSET:
            field_dict["apiKey"] = api_key

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        enabled = d.pop("enabled", UNSET)

        licence_acknowledged = d.pop("licenceAcknowledged", UNSET)

        permission_recorded = d.pop("permissionRecorded", UNSET)

        api_key = d.pop("apiKey", UNSET)

        media_provider_input = cls(
            enabled=enabled,
            licence_acknowledged=licence_acknowledged,
            permission_recorded=permission_recorded,
            api_key=api_key,
        )

        media_provider_input.additional_properties = d
        return media_provider_input

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
