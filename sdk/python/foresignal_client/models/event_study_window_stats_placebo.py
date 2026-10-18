from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="EventStudyWindowStatsPlacebo")


@_attrs_define
class EventStudyWindowStatsPlacebo:
    """Placebo distribution of the same statistic

    Attributes:
        count (int):
        mean (float | None | Unset):
        sd (float | None | Unset):
        p05 (float | None | Unset):
        p50 (float | None | Unset):
        p95 (float | None | Unset):
    """

    count: int
    mean: float | None | Unset = UNSET
    sd: float | None | Unset = UNSET
    p05: float | None | Unset = UNSET
    p50: float | None | Unset = UNSET
    p95: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        count = self.count

        mean: float | None | Unset
        if isinstance(self.mean, Unset):
            mean = UNSET
        else:
            mean = self.mean

        sd: float | None | Unset
        if isinstance(self.sd, Unset):
            sd = UNSET
        else:
            sd = self.sd

        p05: float | None | Unset
        if isinstance(self.p05, Unset):
            p05 = UNSET
        else:
            p05 = self.p05

        p50: float | None | Unset
        if isinstance(self.p50, Unset):
            p50 = UNSET
        else:
            p50 = self.p50

        p95: float | None | Unset
        if isinstance(self.p95, Unset):
            p95 = UNSET
        else:
            p95 = self.p95

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "count": count,
            }
        )
        if mean is not UNSET:
            field_dict["mean"] = mean
        if sd is not UNSET:
            field_dict["sd"] = sd
        if p05 is not UNSET:
            field_dict["p05"] = p05
        if p50 is not UNSET:
            field_dict["p50"] = p50
        if p95 is not UNSET:
            field_dict["p95"] = p95

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        count = d.pop("count")

        def _parse_mean(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        mean = _parse_mean(d.pop("mean", UNSET))

        def _parse_sd(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        sd = _parse_sd(d.pop("sd", UNSET))

        def _parse_p05(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        p05 = _parse_p05(d.pop("p05", UNSET))

        def _parse_p50(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        p50 = _parse_p50(d.pop("p50", UNSET))

        def _parse_p95(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        p95 = _parse_p95(d.pop("p95", UNSET))

        event_study_window_stats_placebo = cls(
            count=count,
            mean=mean,
            sd=sd,
            p05=p05,
            p50=p50,
            p95=p95,
        )

        event_study_window_stats_placebo.additional_properties = d
        return event_study_window_stats_placebo

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
