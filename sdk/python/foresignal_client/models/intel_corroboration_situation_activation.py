from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="IntelCorroborationSituationActivation")


@_attrs_define
class IntelCorroborationSituationActivation:
    """The rule that moves a situation from emerging to active (118)

    Attributes:
        min_families (int | Unset): Distinct signal families activation needs (default 2)
        min_groups (int | Unset): Independent evidence groups activation needs (default 2)
    """

    min_families: int | Unset = UNSET
    min_groups: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        min_families = self.min_families

        min_groups = self.min_groups

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if min_families is not UNSET:
            field_dict["minFamilies"] = min_families
        if min_groups is not UNSET:
            field_dict["minGroups"] = min_groups

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        min_families = d.pop("minFamilies", UNSET)

        min_groups = d.pop("minGroups", UNSET)

        intel_corroboration_situation_activation = cls(
            min_families=min_families,
            min_groups=min_groups,
        )

        intel_corroboration_situation_activation.additional_properties = d
        return intel_corroboration_situation_activation

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
