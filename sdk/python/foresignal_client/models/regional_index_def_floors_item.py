from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.regional_index_def_floors_item_condition import (
        RegionalIndexDefFloorsItemCondition,
    )


T = TypeVar("T", bound="RegionalIndexDefFloorsItem")


@_attrs_define
class RegionalIndexDefFloorsItem:
    """Floor rule

    Attributes:
        description (str):
        min_value (float):
        id (str | Unset):
        condition (RegionalIndexDefFloorsItemCondition | Unset):
    """

    description: str
    min_value: float
    id: str | Unset = UNSET
    condition: RegionalIndexDefFloorsItemCondition | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        description = self.description

        min_value = self.min_value

        id = self.id

        condition: dict[str, Any] | Unset = UNSET
        if not isinstance(self.condition, Unset):
            condition = self.condition.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "description": description,
                "minValue": min_value,
            }
        )
        if id is not UNSET:
            field_dict["id"] = id
        if condition is not UNSET:
            field_dict["condition"] = condition

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.regional_index_def_floors_item_condition import (
            RegionalIndexDefFloorsItemCondition,
        )

        d = dict(src_dict)
        description = d.pop("description")

        min_value = d.pop("minValue")

        id = d.pop("id", UNSET)

        _condition = d.pop("condition", UNSET)
        condition: RegionalIndexDefFloorsItemCondition | Unset
        if isinstance(_condition, Unset):
            condition = UNSET
        else:
            condition = RegionalIndexDefFloorsItemCondition.from_dict(_condition)

        regional_index_def_floors_item = cls(
            description=description,
            min_value=min_value,
            id=id,
            condition=condition,
        )

        regional_index_def_floors_item.additional_properties = d
        return regional_index_def_floors_item

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
