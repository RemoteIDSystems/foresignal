from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.icd_term import IcdTerm
from ..types import UNSET, Unset

T = TypeVar("T", bound="IntelBoardBandChangesItem")


@_attrs_define
class IntelBoardBandChangesItem:
    """Band change in the last 24 h

    Attributes:
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        hypothesis_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        to_term (IcdTerm): ICD 203 likelihood term, derived from the numeric probability
        at (datetime.datetime):
        question (str | Unset):
        label (str | Unset):
        from_term (IcdTerm | Unset): ICD 203 likelihood term, derived from the numeric probability
        probability (float | Unset):
    """

    set_id: str
    hypothesis_id: str
    to_term: IcdTerm
    at: datetime.datetime
    question: str | Unset = UNSET
    label: str | Unset = UNSET
    from_term: IcdTerm | Unset = UNSET
    probability: float | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        set_id = self.set_id

        hypothesis_id = self.hypothesis_id

        to_term = self.to_term.value

        at = self.at.isoformat()

        question = self.question

        label = self.label

        from_term: str | Unset = UNSET
        if not isinstance(self.from_term, Unset):
            from_term = self.from_term.value

        probability = self.probability

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "setId": set_id,
                "hypothesisId": hypothesis_id,
                "toTerm": to_term,
                "at": at,
            }
        )
        if question is not UNSET:
            field_dict["question"] = question
        if label is not UNSET:
            field_dict["label"] = label
        if from_term is not UNSET:
            field_dict["fromTerm"] = from_term
        if probability is not UNSET:
            field_dict["probability"] = probability

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        set_id = d.pop("setId")

        hypothesis_id = d.pop("hypothesisId")

        to_term = IcdTerm(d.pop("toTerm"))

        at = datetime.datetime.fromisoformat(d.pop("at"))

        question = d.pop("question", UNSET)

        label = d.pop("label", UNSET)

        _from_term = d.pop("fromTerm", UNSET)
        from_term: IcdTerm | Unset
        if isinstance(_from_term, Unset):
            from_term = UNSET
        else:
            from_term = IcdTerm(_from_term)

        probability = d.pop("probability", UNSET)

        intel_board_band_changes_item = cls(
            set_id=set_id,
            hypothesis_id=hypothesis_id,
            to_term=to_term,
            at=at,
            question=question,
            label=label,
            from_term=from_term,
            probability=probability,
        )

        intel_board_band_changes_item.additional_properties = d
        return intel_board_band_changes_item

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
