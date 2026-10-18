from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.confidence_level import ConfidenceLevel
from ..models.icd_term import IcdTerm
from ..types import UNSET, Unset

T = TypeVar("T", bound="HypothesisProbabilityState")


@_attrs_define
class HypothesisProbabilityState:
    """A hypothesis's probability, term and confidence at one point

    Attributes:
        hypothesis_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        probability (float):
        estimative_term (IcdTerm): ICD 203 likelihood term, derived from the numeric probability
        label (str | Unset):
        confidence (ConfidenceLevel | Unset): Analytic confidence in the evidence (separate from probability)
    """

    hypothesis_id: str
    probability: float
    estimative_term: IcdTerm
    label: str | Unset = UNSET
    confidence: ConfidenceLevel | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        hypothesis_id = self.hypothesis_id

        probability = self.probability

        estimative_term = self.estimative_term.value

        label = self.label

        confidence: str | Unset = UNSET
        if not isinstance(self.confidence, Unset):
            confidence = self.confidence.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "hypothesisId": hypothesis_id,
                "probability": probability,
                "estimativeTerm": estimative_term,
            }
        )
        if label is not UNSET:
            field_dict["label"] = label
        if confidence is not UNSET:
            field_dict["confidence"] = confidence

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        hypothesis_id = d.pop("hypothesisId")

        probability = d.pop("probability")

        estimative_term = IcdTerm(d.pop("estimativeTerm"))

        label = d.pop("label", UNSET)

        _confidence = d.pop("confidence", UNSET)
        confidence: ConfidenceLevel | Unset
        if isinstance(_confidence, Unset):
            confidence = UNSET
        else:
            confidence = ConfidenceLevel(_confidence)

        hypothesis_probability_state = cls(
            hypothesis_id=hypothesis_id,
            probability=probability,
            estimative_term=estimative_term,
            label=label,
            confidence=confidence,
        )

        hypothesis_probability_state.additional_properties = d
        return hypothesis_probability_state

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
