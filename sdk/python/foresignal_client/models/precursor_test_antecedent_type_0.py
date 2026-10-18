from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.precursor_test_antecedent_type_0_rule import (
        PrecursorTestAntecedentType0Rule,
    )


T = TypeVar("T", bound="PrecursorTestAntecedentType0")


@_attrs_define
class PrecursorTestAntecedentType0:
    """Inline record filter (017 rule fields)

    Attributes:
        rule (PrecursorTestAntecedentType0Rule): Partial TriggerRuleInput: source/kind arrays, conditions, geofence
    """

    rule: PrecursorTestAntecedentType0Rule
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        rule = self.rule.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "rule": rule,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.precursor_test_antecedent_type_0_rule import (
            PrecursorTestAntecedentType0Rule,
        )

        d = dict(src_dict)
        rule = PrecursorTestAntecedentType0Rule.from_dict(d.pop("rule"))

        precursor_test_antecedent_type_0 = cls(
            rule=rule,
        )

        precursor_test_antecedent_type_0.additional_properties = d
        return precursor_test_antecedent_type_0

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
