from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SystemRastersCachesAdditionalProperty")


@_attrs_define
class SystemRastersCachesAdditionalProperty:
    """Cache metrics

    Attributes:
        size_bytes (int | Unset):
        max_bytes (int | Unset):
        hit_rate (float | Unset):
        eviction_rate (float | Unset): Evictions per minute
    """

    size_bytes: int | Unset = UNSET
    max_bytes: int | Unset = UNSET
    hit_rate: float | Unset = UNSET
    eviction_rate: float | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        size_bytes = self.size_bytes

        max_bytes = self.max_bytes

        hit_rate = self.hit_rate

        eviction_rate = self.eviction_rate

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if size_bytes is not UNSET:
            field_dict["sizeBytes"] = size_bytes
        if max_bytes is not UNSET:
            field_dict["maxBytes"] = max_bytes
        if hit_rate is not UNSET:
            field_dict["hitRate"] = hit_rate
        if eviction_rate is not UNSET:
            field_dict["evictionRate"] = eviction_rate

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        size_bytes = d.pop("sizeBytes", UNSET)

        max_bytes = d.pop("maxBytes", UNSET)

        hit_rate = d.pop("hitRate", UNSET)

        eviction_rate = d.pop("evictionRate", UNSET)

        system_rasters_caches_additional_property = cls(
            size_bytes=size_bytes,
            max_bytes=max_bytes,
            hit_rate=hit_rate,
            eviction_rate=eviction_rate,
        )

        system_rasters_caches_additional_property.additional_properties = d
        return system_rasters_caches_additional_property

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
