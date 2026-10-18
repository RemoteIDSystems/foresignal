from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.icd_term import IcdTerm
from ..types import UNSET, Unset

T = TypeVar("T", bound="AchMatrixHypothesesItem")


@_attrs_define
class AchMatrixHypothesesItem:
    """Matrix column

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        label (str):
        probability (float):
        inconsistency_score (float): −Σ w·inc (I = 1, II = 2)
        estimative_term (IcdTerm | Unset): ICD 203 likelihood term, derived from the numeric probability
    """

    id: str
    label: str
    probability: float
    inconsistency_score: float
    estimative_term: IcdTerm | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        label = self.label

        probability = self.probability

        inconsistency_score = self.inconsistency_score

        estimative_term: str | Unset = UNSET
        if not isinstance(self.estimative_term, Unset):
            estimative_term = self.estimative_term.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "label": label,
                "probability": probability,
                "inconsistencyScore": inconsistency_score,
            }
        )
        if estimative_term is not UNSET:
            field_dict["estimativeTerm"] = estimative_term

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        label = d.pop("label")

        probability = d.pop("probability")

        inconsistency_score = d.pop("inconsistencyScore")

        _estimative_term = d.pop("estimativeTerm", UNSET)
        estimative_term: IcdTerm | Unset
        if isinstance(_estimative_term, Unset):
            estimative_term = UNSET
        else:
            estimative_term = IcdTerm(_estimative_term)

        ach_matrix_hypotheses_item = cls(
            id=id,
            label=label,
            probability=probability,
            inconsistency_score=inconsistency_score,
            estimative_term=estimative_term,
        )

        ach_matrix_hypotheses_item.additional_properties = d
        return ach_matrix_hypotheses_item

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
