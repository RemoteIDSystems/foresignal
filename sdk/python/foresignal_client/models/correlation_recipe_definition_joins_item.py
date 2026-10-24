from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.correlation_recipe_definition_joins_item_role import (
    CorrelationRecipeDefinitionJoinsItemRole,
)
from ..models.correlation_recipe_definition_joins_item_same_region import (
    CorrelationRecipeDefinitionJoinsItemSameRegion,
)
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.condition import Condition
    from ..models.correlation_recipe_definition_joins_item_window import (
        CorrelationRecipeDefinitionJoinsItemWindow,
    )


T = TypeVar("T", bound="CorrelationRecipeDefinitionJoinsItem")


@_attrs_define
class CorrelationRecipeDefinitionJoinsItem:
    """A join clause

    Attributes:
        kinds (list[str] | Unset):
        conditions (list[Condition] | Unset):
        within_km (float | Unset):
        same_region (CorrelationRecipeDefinitionJoinsItemSameRegion | Unset):
        same_entity (bool | Unset):
        shared_attribute (str | Unset): A payload dot-path (prompt 157); a candidate joins when its value at that path
            equals a seed member's — the join a fraud/relationship recipe needs (address, phone, officer, shared id).
            Matches on the attribute alone; geometry is not required, the time window still applies. Example:
            registeredAddress.
        window (CorrelationRecipeDefinitionJoinsItemWindow | Unset): Time window around the seed
        role (CorrelationRecipeDefinitionJoinsItemRole | Unset):
        signal_family (str | Unset): Signal family from the shared registry (`intel/signal-families.json`, 119), e.g.
            `ais-behaviour`, `nav-warning`, `internet-measurement` Example: ais-behaviour.
        weight (float | Unset):
    """

    kinds: list[str] | Unset = UNSET
    conditions: list[Condition] | Unset = UNSET
    within_km: float | Unset = UNSET
    same_region: CorrelationRecipeDefinitionJoinsItemSameRegion | Unset = UNSET
    same_entity: bool | Unset = UNSET
    shared_attribute: str | Unset = UNSET
    window: CorrelationRecipeDefinitionJoinsItemWindow | Unset = UNSET
    role: CorrelationRecipeDefinitionJoinsItemRole | Unset = UNSET
    signal_family: str | Unset = UNSET
    weight: float | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        kinds: list[str] | Unset = UNSET
        if not isinstance(self.kinds, Unset):
            kinds = self.kinds

        conditions: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.conditions, Unset):
            conditions = []
            for conditions_item_data in self.conditions:
                conditions_item = conditions_item_data.to_dict()
                conditions.append(conditions_item)

        within_km = self.within_km

        same_region: str | Unset = UNSET
        if not isinstance(self.same_region, Unset):
            same_region = self.same_region.value

        same_entity = self.same_entity

        shared_attribute = self.shared_attribute

        window: dict[str, Any] | Unset = UNSET
        if not isinstance(self.window, Unset):
            window = self.window.to_dict()

        role: str | Unset = UNSET
        if not isinstance(self.role, Unset):
            role = self.role.value

        signal_family = self.signal_family

        weight = self.weight

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if kinds is not UNSET:
            field_dict["kinds"] = kinds
        if conditions is not UNSET:
            field_dict["conditions"] = conditions
        if within_km is not UNSET:
            field_dict["withinKm"] = within_km
        if same_region is not UNSET:
            field_dict["sameRegion"] = same_region
        if same_entity is not UNSET:
            field_dict["sameEntity"] = same_entity
        if shared_attribute is not UNSET:
            field_dict["sharedAttribute"] = shared_attribute
        if window is not UNSET:
            field_dict["window"] = window
        if role is not UNSET:
            field_dict["role"] = role
        if signal_family is not UNSET:
            field_dict["signalFamily"] = signal_family
        if weight is not UNSET:
            field_dict["weight"] = weight

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.condition import Condition
        from ..models.correlation_recipe_definition_joins_item_window import (
            CorrelationRecipeDefinitionJoinsItemWindow,
        )

        d = dict(src_dict)
        kinds = cast(list[str], d.pop("kinds", UNSET))

        _conditions = d.pop("conditions", UNSET)
        conditions: list[Condition] | Unset = UNSET
        if _conditions is not UNSET:
            conditions = []
            for conditions_item_data in _conditions:
                conditions_item = Condition.from_dict(conditions_item_data)

                conditions.append(conditions_item)

        within_km = d.pop("withinKm", UNSET)

        _same_region = d.pop("sameRegion", UNSET)
        same_region: CorrelationRecipeDefinitionJoinsItemSameRegion | Unset
        if isinstance(_same_region, Unset):
            same_region = UNSET
        else:
            same_region = CorrelationRecipeDefinitionJoinsItemSameRegion(_same_region)

        same_entity = d.pop("sameEntity", UNSET)

        shared_attribute = d.pop("sharedAttribute", UNSET)

        _window = d.pop("window", UNSET)
        window: CorrelationRecipeDefinitionJoinsItemWindow | Unset
        if isinstance(_window, Unset):
            window = UNSET
        else:
            window = CorrelationRecipeDefinitionJoinsItemWindow.from_dict(_window)

        _role = d.pop("role", UNSET)
        role: CorrelationRecipeDefinitionJoinsItemRole | Unset
        if isinstance(_role, Unset):
            role = UNSET
        else:
            role = CorrelationRecipeDefinitionJoinsItemRole(_role)

        signal_family = d.pop("signalFamily", UNSET)

        weight = d.pop("weight", UNSET)

        correlation_recipe_definition_joins_item = cls(
            kinds=kinds,
            conditions=conditions,
            within_km=within_km,
            same_region=same_region,
            same_entity=same_entity,
            shared_attribute=shared_attribute,
            window=window,
            role=role,
            signal_family=signal_family,
            weight=weight,
        )

        correlation_recipe_definition_joins_item.additional_properties = d
        return correlation_recipe_definition_joins_item

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
