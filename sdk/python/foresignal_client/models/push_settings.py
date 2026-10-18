from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="PushSettings")


@_attrs_define
class PushSettings:
    """Push settings inside ServerSettings (031)

    Attributes:
        contact (str | Unset): VAPID `sub` Default: 'mailto:admin@localhost'.
        allowed_private_hosts (list[str] | Unset): Exact hostnames or CIDRs (IPv4 prefix ≥ /8, IPv6 ≥ /32) allowed as
            push endpoints despite resolving to private addresses (e.g. a LAN UnifiedPush distributor); https still required
        default_lang (str | Unset):  Default: 'en'.
    """

    contact: str | Unset = "mailto:admin@localhost"
    allowed_private_hosts: list[str] | Unset = UNSET
    default_lang: str | Unset = "en"
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        contact = self.contact

        allowed_private_hosts: list[str] | Unset = UNSET
        if not isinstance(self.allowed_private_hosts, Unset):
            allowed_private_hosts = self.allowed_private_hosts

        default_lang = self.default_lang

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if contact is not UNSET:
            field_dict["contact"] = contact
        if allowed_private_hosts is not UNSET:
            field_dict["allowedPrivateHosts"] = allowed_private_hosts
        if default_lang is not UNSET:
            field_dict["defaultLang"] = default_lang

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        contact = d.pop("contact", UNSET)

        allowed_private_hosts = cast(list[str], d.pop("allowedPrivateHosts", UNSET))

        default_lang = d.pop("defaultLang", UNSET)

        push_settings = cls(
            contact=contact,
            allowed_private_hosts=allowed_private_hosts,
            default_lang=default_lang,
        )

        push_settings.additional_properties = d
        return push_settings

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
