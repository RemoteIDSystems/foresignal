from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.frame_stats_code_counts import FrameStatsCodeCounts


T = TypeVar("T", bound="FrameStats")


@_attrs_define
class FrameStats:
    """Frame statistics in physical units

    Attributes:
        min_ (float | None | Unset):
        max_ (float | None | Unset):
        mean (float | None | Unset):
        coverage_pct (float | Unset): Share of grid cells with coverage
        code_counts (FrameStatsCodeCounts | Unset): Per-code counts for categorical products (raw code → cells)
        flag_missing (bool | Unset): mrms-precip-rate: the PrecipFlag band was missing (committed after 4 min)
    """

    min_: float | None | Unset = UNSET
    max_: float | None | Unset = UNSET
    mean: float | None | Unset = UNSET
    coverage_pct: float | Unset = UNSET
    code_counts: FrameStatsCodeCounts | Unset = UNSET
    flag_missing: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        min_: float | None | Unset
        if isinstance(self.min_, Unset):
            min_ = UNSET
        else:
            min_ = self.min_

        max_: float | None | Unset
        if isinstance(self.max_, Unset):
            max_ = UNSET
        else:
            max_ = self.max_

        mean: float | None | Unset
        if isinstance(self.mean, Unset):
            mean = UNSET
        else:
            mean = self.mean

        coverage_pct = self.coverage_pct

        code_counts: dict[str, Any] | Unset = UNSET
        if not isinstance(self.code_counts, Unset):
            code_counts = self.code_counts.to_dict()

        flag_missing = self.flag_missing

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if min_ is not UNSET:
            field_dict["min"] = min_
        if max_ is not UNSET:
            field_dict["max"] = max_
        if mean is not UNSET:
            field_dict["mean"] = mean
        if coverage_pct is not UNSET:
            field_dict["coveragePct"] = coverage_pct
        if code_counts is not UNSET:
            field_dict["codeCounts"] = code_counts
        if flag_missing is not UNSET:
            field_dict["flagMissing"] = flag_missing

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.frame_stats_code_counts import FrameStatsCodeCounts

        d = dict(src_dict)

        def _parse_min_(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        min_ = _parse_min_(d.pop("min", UNSET))

        def _parse_max_(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        max_ = _parse_max_(d.pop("max", UNSET))

        def _parse_mean(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        mean = _parse_mean(d.pop("mean", UNSET))

        coverage_pct = d.pop("coveragePct", UNSET)

        _code_counts = d.pop("codeCounts", UNSET)
        code_counts: FrameStatsCodeCounts | Unset
        if isinstance(_code_counts, Unset):
            code_counts = UNSET
        else:
            code_counts = FrameStatsCodeCounts.from_dict(_code_counts)

        flag_missing = d.pop("flagMissing", UNSET)

        frame_stats = cls(
            min_=min_,
            max_=max_,
            mean=mean,
            coverage_pct=coverage_pct,
            code_counts=code_counts,
            flag_missing=flag_missing,
        )

        frame_stats.additional_properties = d
        return frame_stats

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
