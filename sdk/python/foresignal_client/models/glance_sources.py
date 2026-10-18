from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="GlanceSources")


@_attrs_define
class GlanceSources:
    """Source health tallies

    Attributes:
        healthy (int | Unset):
        degraded (int | Unset):
        failing (int | Unset):
    """

    healthy: int | Unset = UNSET
    degraded: int | Unset = UNSET
    failing: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        healthy = self.healthy

        degraded = self.degraded

        failing = self.failing

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
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
        healthy = d.pop("healthy", UNSET)

        degraded = d.pop("degraded", UNSET)

        failing = d.pop("failing", UNSET)

        glance_sources = cls(
            healthy=healthy,
            degraded=degraded,
            failing=failing,
        )

        glance_sources.additional_properties = d
        return glance_sources

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
