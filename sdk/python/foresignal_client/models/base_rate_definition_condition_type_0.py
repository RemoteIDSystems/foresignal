from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="BaseRateDefinitionConditionType0")


@_attrs_define
class BaseRateDefinitionConditionType0:
    """Conditional rate: P(event in window | antecedent family occurred)

    Attributes:
        family (str):
        within_days (int):
    """

    family: str
    within_days: int
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        family = self.family

        within_days = self.within_days

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "family": family,
                "withinDays": within_days,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        family = d.pop("family")

        within_days = d.pop("withinDays")

        base_rate_definition_condition_type_0 = cls(
            family=family,
            within_days=within_days,
        )

        base_rate_definition_condition_type_0.additional_properties = d
        return base_rate_definition_condition_type_0

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
