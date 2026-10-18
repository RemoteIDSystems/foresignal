from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ScorecardEntryIntervalCoverage")


@_attrs_define
class ScorecardEntryIntervalCoverage:
    """Share of numeric outcomes inside central intervals

    Attributes:
        p05p95 (float | None | Unset):
        p25p75 (float | None | Unset):
    """

    p05p95: float | None | Unset = UNSET
    p25p75: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        p05p95: float | None | Unset
        if isinstance(self.p05p95, Unset):
            p05p95 = UNSET
        else:
            p05p95 = self.p05p95

        p25p75: float | None | Unset
        if isinstance(self.p25p75, Unset):
            p25p75 = UNSET
        else:
            p25p75 = self.p25p75

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if p05p95 is not UNSET:
            field_dict["p05p95"] = p05p95
        if p25p75 is not UNSET:
            field_dict["p25p75"] = p25p75

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_p05p95(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        p05p95 = _parse_p05p95(d.pop("p05p95", UNSET))

        def _parse_p25p75(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        p25p75 = _parse_p25p75(d.pop("p25p75", UNSET))

        scorecard_entry_interval_coverage = cls(
            p05p95=p05p95,
            p25p75=p25p75,
        )

        scorecard_entry_interval_coverage.additional_properties = d
        return scorecard_entry_interval_coverage

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
