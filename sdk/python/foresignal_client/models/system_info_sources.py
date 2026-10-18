from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SystemInfoSources")


@_attrs_define
class SystemInfoSources:
    """
    Attributes:
        total (int | Unset):
        enabled (int | Unset):
        healthy (int | Unset):
        degraded (int | Unset):
        failing (int | Unset):
    """

    total: int | Unset = UNSET
    enabled: int | Unset = UNSET
    healthy: int | Unset = UNSET
    degraded: int | Unset = UNSET
    failing: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        total = self.total

        enabled = self.enabled

        healthy = self.healthy

        degraded = self.degraded

        failing = self.failing

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if total is not UNSET:
            field_dict["total"] = total
        if enabled is not UNSET:
            field_dict["enabled"] = enabled
        if healthy is not UNSET:
            field_dict["healthy"] = healthy
        if degraded is not UNSET:
            field_dict["degraded"] = degraded
        if failing is not UNSET:
            field_dict["failing"] = failing

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        total = d.pop("total", UNSET)

        enabled = d.pop("enabled", UNSET)

        healthy = d.pop("healthy", UNSET)

        degraded = d.pop("degraded", UNSET)

        failing = d.pop("failing", UNSET)

        system_info_sources = cls(
            total=total,
            enabled=enabled,
            healthy=healthy,
            degraded=degraded,
            failing=failing,
        )

        system_info_sources.additional_properties = d
        return system_info_sources

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
