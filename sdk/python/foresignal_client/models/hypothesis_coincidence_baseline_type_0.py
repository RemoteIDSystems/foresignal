from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="HypothesisCoincidenceBaselineType0")


@_attrs_define
class HypothesisCoincidenceBaselineType0:
    """Coincidence baseline from the linked event study's placebo distribution

    Attributes:
        placebo_rate (float):
        method (str):  Example: placebo-event-study.
        event_study_id (None | str | Unset):
        n_placebo (int | None | Unset):
    """

    placebo_rate: float
    method: str
    event_study_id: None | str | Unset = UNSET
    n_placebo: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        placebo_rate = self.placebo_rate

        method = self.method

        event_study_id: None | str | Unset
        if isinstance(self.event_study_id, Unset):
            event_study_id = UNSET
        else:
            event_study_id = self.event_study_id

        n_placebo: int | None | Unset
        if isinstance(self.n_placebo, Unset):
            n_placebo = UNSET
        else:
            n_placebo = self.n_placebo

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "placeboRate": placebo_rate,
                "method": method,
            }
        )
        if event_study_id is not UNSET:
            field_dict["eventStudyId"] = event_study_id
        if n_placebo is not UNSET:
            field_dict["nPlacebo"] = n_placebo

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        placebo_rate = d.pop("placeboRate")

        method = d.pop("method")

        def _parse_event_study_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        event_study_id = _parse_event_study_id(d.pop("eventStudyId", UNSET))

        def _parse_n_placebo(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        n_placebo = _parse_n_placebo(d.pop("nPlacebo", UNSET))

        hypothesis_coincidence_baseline_type_0 = cls(
            placebo_rate=placebo_rate,
            method=method,
            event_study_id=event_study_id,
            n_placebo=n_placebo,
        )

        hypothesis_coincidence_baseline_type_0.additional_properties = d
        return hypothesis_coincidence_baseline_type_0

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
