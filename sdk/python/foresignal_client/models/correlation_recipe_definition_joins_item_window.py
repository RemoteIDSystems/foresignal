from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="CorrelationRecipeDefinitionJoinsItemWindow")


@_attrs_define
class CorrelationRecipeDefinitionJoinsItemWindow:
    """Time window around the seed

    Attributes:
        before (str | Unset): Before the seed. ISO-8601 duration or shorthand (30m, 6h, 7d).
        after (str | Unset): After the seed. ISO-8601 duration or shorthand (30m, 6h, 7d).
    """

    before: str | Unset = UNSET
    after: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        before = self.before

        after = self.after

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if before is not UNSET:
            field_dict["before"] = before
        if after is not UNSET:
            field_dict["after"] = after

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        before = d.pop("before", UNSET)

        after = d.pop("after", UNSET)

        correlation_recipe_definition_joins_item_window = cls(
            before=before,
            after=after,
        )

        correlation_recipe_definition_joins_item_window.additional_properties = d
        return correlation_recipe_definition_joins_item_window

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
