from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="OutboundSettings")


@_attrs_define
class OutboundSettings:
    """Admin overrides of the outbound URL policy (035); deny wins

    Attributes:
        allow_hosts (list[str] | Unset): Hosts allowed even when they resolve to addresses the profile would refuse
            (never the always-blocked set)
        allow_cidrs (list[str] | Unset): Address ranges allowed in every profile (never the always-blocked set)
        deny_hosts (list[str] | Unset): Hosts refused in every profile
        deny_cidrs (list[str] | Unset): Address ranges refused in every profile
    """

    allow_hosts: list[str] | Unset = UNSET
    allow_cidrs: list[str] | Unset = UNSET
    deny_hosts: list[str] | Unset = UNSET
    deny_cidrs: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        allow_hosts: list[str] | Unset = UNSET
        if not isinstance(self.allow_hosts, Unset):
            allow_hosts = self.allow_hosts

        allow_cidrs: list[str] | Unset = UNSET
        if not isinstance(self.allow_cidrs, Unset):
            allow_cidrs = self.allow_cidrs

        deny_hosts: list[str] | Unset = UNSET
        if not isinstance(self.deny_hosts, Unset):
            deny_hosts = self.deny_hosts

        deny_cidrs: list[str] | Unset = UNSET
        if not isinstance(self.deny_cidrs, Unset):
            deny_cidrs = self.deny_cidrs

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if allow_hosts is not UNSET:
            field_dict["allowHosts"] = allow_hosts
        if allow_cidrs is not UNSET:
            field_dict["allowCidrs"] = allow_cidrs
        if deny_hosts is not UNSET:
            field_dict["denyHosts"] = deny_hosts
        if deny_cidrs is not UNSET:
            field_dict["denyCidrs"] = deny_cidrs

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        allow_hosts = cast(list[str], d.pop("allowHosts", UNSET))

        allow_cidrs = cast(list[str], d.pop("allowCidrs", UNSET))

        deny_hosts = cast(list[str], d.pop("denyHosts", UNSET))

        deny_cidrs = cast(list[str], d.pop("denyCidrs", UNSET))

        outbound_settings = cls(
            allow_hosts=allow_hosts,
            allow_cidrs=allow_cidrs,
            deny_hosts=deny_hosts,
            deny_cidrs=deny_cidrs,
        )

        outbound_settings.additional_properties = d
        return outbound_settings

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
