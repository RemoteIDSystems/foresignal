from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="CorrelationRecipeDefinitionDedup")


@_attrs_define
class CorrelationRecipeDefinitionDedup:
    """Same-event dedup

    Attributes:
        radius_km (float | Unset):
        window (str | Unset): Overlap window. ISO-8601 duration or shorthand (30m, 6h, 7d).
    """

    radius_km: float | Unset = UNSET
    window: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        radius_km = self.radius_km

        window = self.window

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if radius_km is not UNSET:
            field_dict["radiusKm"] = radius_km
        if window is not UNSET:
            field_dict["window"] = window

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        radius_km = d.pop("radiusKm", UNSET)

        window = d.pop("window", UNSET)

        correlation_recipe_definition_dedup = cls(
            radius_km=radius_km,
            window=window,
        )

        correlation_recipe_definition_dedup.additional_properties = d
        return correlation_recipe_definition_dedup

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
