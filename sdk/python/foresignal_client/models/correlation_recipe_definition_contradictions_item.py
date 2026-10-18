from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="CorrelationRecipeDefinitionContradictionsItem")


@_attrs_define
class CorrelationRecipeDefinitionContradictionsItem:
    """A contradiction clause that adds an alternative and lowers confidence

    Attributes:
        alternative (str):
        kinds (list[str] | Unset):
        health_of (list[str] | Unset): Source ids whose health or coverage drop counts
        within_km (float | Unset):
        window (str | Unset): Window. ISO-8601 duration or shorthand (30m, 6h, 7d).
    """

    alternative: str
    kinds: list[str] | Unset = UNSET
    health_of: list[str] | Unset = UNSET
    within_km: float | Unset = UNSET
    window: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        alternative = self.alternative

        kinds: list[str] | Unset = UNSET
        if not isinstance(self.kinds, Unset):
            kinds = self.kinds

        health_of: list[str] | Unset = UNSET
        if not isinstance(self.health_of, Unset):
            health_of = self.health_of

        within_km = self.within_km

        window = self.window

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "alternative": alternative,
            }
        )
        if kinds is not UNSET:
            field_dict["kinds"] = kinds
        if health_of is not UNSET:
            field_dict["healthOf"] = health_of
        if within_km is not UNSET:
            field_dict["withinKm"] = within_km
        if window is not UNSET:
            field_dict["window"] = window

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        alternative = d.pop("alternative")

        kinds = cast(list[str], d.pop("kinds", UNSET))

        health_of = cast(list[str], d.pop("healthOf", UNSET))

        within_km = d.pop("withinKm", UNSET)

        window = d.pop("window", UNSET)

        correlation_recipe_definition_contradictions_item = cls(
            alternative=alternative,
            kinds=kinds,
            health_of=health_of,
            within_km=within_km,
            window=window,
        )

        correlation_recipe_definition_contradictions_item.additional_properties = d
        return correlation_recipe_definition_contradictions_item

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
