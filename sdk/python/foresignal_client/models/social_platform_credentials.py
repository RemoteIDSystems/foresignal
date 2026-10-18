from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SocialPlatformCredentials")


@_attrs_define
class SocialPlatformCredentials:
    """Credential status (values never returned)

    Attributes:
        required (bool):
        configured (bool):
        fields (list[str] | Unset): Credential field names, e.g. `bearerToken`, `apiKey`, `apiId`
    """

    required: bool
    configured: bool
    fields: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        required = self.required

        configured = self.configured

        fields: list[str] | Unset = UNSET
        if not isinstance(self.fields, Unset):
            fields = self.fields

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "required": required,
                "configured": configured,
            }
        )
        if fields is not UNSET:
            field_dict["fields"] = fields

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        required = d.pop("required")

        configured = d.pop("configured")

        fields = cast(list[str], d.pop("fields", UNSET))

        social_platform_credentials = cls(
            required=required,
            configured=configured,
            fields=fields,
        )

        social_platform_credentials.additional_properties = d
        return social_platform_credentials

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
