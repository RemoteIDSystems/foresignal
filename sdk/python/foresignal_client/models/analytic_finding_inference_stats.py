from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="AnalyticFindingInferenceStats")


@_attrs_define
class AnalyticFindingInferenceStats:
    """Method numbers by type: change-point `{changeAt, levelBefore, levelAfter, effect, detectionDelay, bocpdPosterior}`;
    pre-event-signal `{car, z, placeboP, corradoP, window, linkedEventId}`; unexplained-move `{z, linkedEventId,
    leadDays}`; co-occurrence `{families, pValues, fisherP, adjustedP, expectedJointRate, lift}`; precursor-active
    `{precursorId, expectedFrom, expectedTo, probability}`

    """

    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        analytic_finding_inference_stats = cls()

        analytic_finding_inference_stats.additional_properties = d
        return analytic_finding_inference_stats

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
