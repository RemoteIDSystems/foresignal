from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="LeadTimeSummary")


@_attrs_define
class LeadTimeSummary:
    """Median and interquartile lead in seconds (positive = social first)

    Attributes:
        n (int):
        median (float | None | Unset):
        p25 (float | None | Unset):
        p75 (float | None | Unset):
    """

    n: int
    median: float | None | Unset = UNSET
    p25: float | None | Unset = UNSET
    p75: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        n = self.n

        median: float | None | Unset
        if isinstance(self.median, Unset):
            median = UNSET
        else:
            median = self.median

        p25: float | None | Unset
        if isinstance(self.p25, Unset):
            p25 = UNSET
        else:
            p25 = self.p25

        p75: float | None | Unset
        if isinstance(self.p75, Unset):
            p75 = UNSET
        else:
            p75 = self.p75

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "n": n,
            }
        )
        if median is not UNSET:
            field_dict["median"] = median
        if p25 is not UNSET:
            field_dict["p25"] = p25
        if p75 is not UNSET:
            field_dict["p75"] = p75

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        n = d.pop("n")

        def _parse_median(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        median = _parse_median(d.pop("median", UNSET))

        def _parse_p25(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        p25 = _parse_p25(d.pop("p25", UNSET))

        def _parse_p75(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        p75 = _parse_p75(d.pop("p75", UNSET))

        lead_time_summary = cls(
            n=n,
            median=median,
            p25=p25,
            p75=p75,
        )

        lead_time_summary.additional_properties = d
        return lead_time_summary

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
