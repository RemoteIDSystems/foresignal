from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.icd_term import IcdTerm
from ..types import UNSET, Unset

T = TypeVar("T", bound="HypothesisSetEventBandChangesItem")


@_attrs_define
class HypothesisSetEventBandChangesItem:
    """Band change

    Attributes:
        hypothesis_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        from_term (IcdTerm): ICD 203 likelihood term, derived from the numeric probability
        to_term (IcdTerm): ICD 203 likelihood term, derived from the numeric probability
        probability (float):
        label (str | Unset):
    """

    hypothesis_id: str
    from_term: IcdTerm
    to_term: IcdTerm
    probability: float
    label: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        hypothesis_id = self.hypothesis_id

        from_term = self.from_term.value

        to_term = self.to_term.value

        probability = self.probability

        label = self.label

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "hypothesisId": hypothesis_id,
                "fromTerm": from_term,
                "toTerm": to_term,
                "probability": probability,
            }
        )
        if label is not UNSET:
            field_dict["label"] = label

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        hypothesis_id = d.pop("hypothesisId")

        from_term = IcdTerm(d.pop("fromTerm"))

        to_term = IcdTerm(d.pop("toTerm"))

        probability = d.pop("probability")

        label = d.pop("label", UNSET)

        hypothesis_set_event_band_changes_item = cls(
            hypothesis_id=hypothesis_id,
            from_term=from_term,
            to_term=to_term,
            probability=probability,
            label=label,
        )

        hypothesis_set_event_band_changes_item.additional_properties = d
        return hypothesis_set_event_band_changes_item

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
