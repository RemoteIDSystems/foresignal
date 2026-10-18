from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="PrecursorTestInputConsequentType1")


@_attrs_define
class PrecursorTestInputConsequentType1:
    """Alerts of a rule

    Attributes:
        alert_rule_id (UUID):
    """

    alert_rule_id: UUID
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        alert_rule_id = str(self.alert_rule_id)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "alertRuleId": alert_rule_id,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        alert_rule_id = UUID(d.pop("alertRuleId"))

        precursor_test_input_consequent_type_1 = cls(
            alert_rule_id=alert_rule_id,
        )

        precursor_test_input_consequent_type_1.additional_properties = d
        return precursor_test_input_consequent_type_1

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
