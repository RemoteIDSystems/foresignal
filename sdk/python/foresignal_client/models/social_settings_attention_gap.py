from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.social_settings_attention_gap_min_severity import (
    SocialSettingsAttentionGapMinSeverity,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="SocialSettingsAttentionGap")


@_attrs_define
class SocialSettingsAttentionGap:
    """Attention-gap detection

    Attributes:
        min_severity (SocialSettingsAttentionGapMinSeverity | Unset): Minimum finding severity; situation escalations
            and critical alerts always qualify Default: SocialSettingsAttentionGapMinSeverity.SIGNIFICANT.
        percentile (float | Unset):  Default: 10.0.
        lookback_days (int | Unset):  Default: 365.
        window_hours (int | Unset):  Default: 6.
    """

    min_severity: SocialSettingsAttentionGapMinSeverity | Unset = (
        SocialSettingsAttentionGapMinSeverity.SIGNIFICANT
    )
    percentile: float | Unset = 10.0
    lookback_days: int | Unset = 365
    window_hours: int | Unset = 6
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        min_severity: str | Unset = UNSET
        if not isinstance(self.min_severity, Unset):
            min_severity = self.min_severity.value

        percentile = self.percentile

        lookback_days = self.lookback_days

        window_hours = self.window_hours

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if min_severity is not UNSET:
            field_dict["minSeverity"] = min_severity
        if percentile is not UNSET:
            field_dict["percentile"] = percentile
        if lookback_days is not UNSET:
            field_dict["lookbackDays"] = lookback_days
        if window_hours is not UNSET:
            field_dict["windowHours"] = window_hours

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        _min_severity = d.pop("minSeverity", UNSET)
        min_severity: SocialSettingsAttentionGapMinSeverity | Unset
        if isinstance(_min_severity, Unset):
            min_severity = UNSET
        else:
            min_severity = SocialSettingsAttentionGapMinSeverity(_min_severity)

        percentile = d.pop("percentile", UNSET)

        lookback_days = d.pop("lookbackDays", UNSET)

        window_hours = d.pop("windowHours", UNSET)

        social_settings_attention_gap = cls(
            min_severity=min_severity,
            percentile=percentile,
            lookback_days=lookback_days,
            window_hours=window_hours,
        )

        social_settings_attention_gap.additional_properties = d
        return social_settings_attention_gap

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
