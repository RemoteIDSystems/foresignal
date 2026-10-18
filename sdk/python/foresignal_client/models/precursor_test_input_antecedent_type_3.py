from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.precursor_test_input_antecedent_type_3_classification import (
    PrecursorTestInputAntecedentType3Classification,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="PrecursorTestInputAntecedentType3")


@_attrs_define
class PrecursorTestInputAntecedentType3:
    """Anomalies of a rule

    Attributes:
        anomaly_rule_id (UUID):
        classification (PrecursorTestInputAntecedentType3Classification | Unset):
    """

    anomaly_rule_id: UUID
    classification: PrecursorTestInputAntecedentType3Classification | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        anomaly_rule_id = str(self.anomaly_rule_id)

        classification: str | Unset = UNSET
        if not isinstance(self.classification, Unset):
            classification = self.classification.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "anomalyRuleId": anomaly_rule_id,
            }
        )
        if classification is not UNSET:
            field_dict["classification"] = classification

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        anomaly_rule_id = UUID(d.pop("anomalyRuleId"))

        _classification = d.pop("classification", UNSET)
        classification: PrecursorTestInputAntecedentType3Classification | Unset
        if isinstance(_classification, Unset):
            classification = UNSET
        else:
            classification = PrecursorTestInputAntecedentType3Classification(
                _classification
            )

        precursor_test_input_antecedent_type_3 = cls(
            anomaly_rule_id=anomaly_rule_id,
            classification=classification,
        )

        precursor_test_input_antecedent_type_3.additional_properties = d
        return precursor_test_input_antecedent_type_3

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
