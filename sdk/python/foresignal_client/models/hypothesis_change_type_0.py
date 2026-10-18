from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.icd_term import IcdTerm
from ..types import UNSET, Unset

T = TypeVar("T", bound="HypothesisChangeType0")


@_attrs_define
class HypothesisChangeType0:
    """Change since the previous revision

    Attributes:
        previous_probability (float):
        delta (float):
        previous_term (IcdTerm | Unset): ICD 203 likelihood term, derived from the numeric probability
        since_revision_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
    """

    previous_probability: float
    delta: float
    previous_term: IcdTerm | Unset = UNSET
    since_revision_id: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        previous_probability = self.previous_probability

        delta = self.delta

        previous_term: str | Unset = UNSET
        if not isinstance(self.previous_term, Unset):
            previous_term = self.previous_term.value

        since_revision_id = self.since_revision_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "previousProbability": previous_probability,
                "delta": delta,
            }
        )
        if previous_term is not UNSET:
            field_dict["previousTerm"] = previous_term
        if since_revision_id is not UNSET:
            field_dict["sinceRevisionId"] = since_revision_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        previous_probability = d.pop("previousProbability")

        delta = d.pop("delta")

        _previous_term = d.pop("previousTerm", UNSET)
        previous_term: IcdTerm | Unset
        if isinstance(_previous_term, Unset):
            previous_term = UNSET
        else:
            previous_term = IcdTerm(_previous_term)

        since_revision_id = d.pop("sinceRevisionId", UNSET)

        hypothesis_change_type_0 = cls(
            previous_probability=previous_probability,
            delta=delta,
            previous_term=previous_term,
            since_revision_id=since_revision_id,
        )

        hypothesis_change_type_0.additional_properties = d
        return hypothesis_change_type_0

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
