from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="RuleQualityDailyFeedback")


@_attrs_define
class RuleQualityDailyFeedback:
    """Counts per feedback value

    Attributes:
        useful (int | Unset):
        noise (int | Unset):
        late (int | Unset):
        false_positive (int | Unset):
    """

    useful: int | Unset = UNSET
    noise: int | Unset = UNSET
    late: int | Unset = UNSET
    false_positive: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        useful = self.useful

        noise = self.noise

        late = self.late

        false_positive = self.false_positive

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if useful is not UNSET:
            field_dict["useful"] = useful
        if noise is not UNSET:
            field_dict["noise"] = noise
        if late is not UNSET:
            field_dict["late"] = late
        if false_positive is not UNSET:
            field_dict["false_positive"] = false_positive

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        useful = d.pop("useful", UNSET)

        noise = d.pop("noise", UNSET)

        late = d.pop("late", UNSET)

        false_positive = d.pop("false_positive", UNSET)

        rule_quality_daily_feedback = cls(
            useful=useful,
            noise=noise,
            late=late,
            false_positive=false_positive,
        )

        rule_quality_daily_feedback.additional_properties = d
        return rule_quality_daily_feedback

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
