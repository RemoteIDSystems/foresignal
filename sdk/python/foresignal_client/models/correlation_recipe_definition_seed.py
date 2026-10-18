from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.condition import Condition


T = TypeVar("T", bound="CorrelationRecipeDefinitionSeed")


@_attrs_define
class CorrelationRecipeDefinitionSeed:
    """Seed records

    Attributes:
        kinds (list[str] | Unset):
        sources (list[str] | Unset):
        conditions (list[Condition] | Unset):
    """

    kinds: list[str] | Unset = UNSET
    sources: list[str] | Unset = UNSET
    conditions: list[Condition] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        kinds: list[str] | Unset = UNSET
        if not isinstance(self.kinds, Unset):
            kinds = self.kinds

        sources: list[str] | Unset = UNSET
        if not isinstance(self.sources, Unset):
            sources = self.sources

        conditions: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.conditions, Unset):
            conditions = []
            for conditions_item_data in self.conditions:
                conditions_item = conditions_item_data.to_dict()
                conditions.append(conditions_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if kinds is not UNSET:
            field_dict["kinds"] = kinds
        if sources is not UNSET:
            field_dict["sources"] = sources
        if conditions is not UNSET:
            field_dict["conditions"] = conditions

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.condition import Condition

        d = dict(src_dict)
        kinds = cast(list[str], d.pop("kinds", UNSET))

        sources = cast(list[str], d.pop("sources", UNSET))

        _conditions = d.pop("conditions", UNSET)
        conditions: list[Condition] | Unset = UNSET
        if _conditions is not UNSET:
            conditions = []
            for conditions_item_data in _conditions:
                conditions_item = Condition.from_dict(conditions_item_data)

                conditions.append(conditions_item)

        correlation_recipe_definition_seed = cls(
            kinds=kinds,
            sources=sources,
            conditions=conditions,
        )

        correlation_recipe_definition_seed.additional_properties = d
        return correlation_recipe_definition_seed

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
