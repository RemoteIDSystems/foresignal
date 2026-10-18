from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ForecastScoreIntervalHitsType0")


@_attrs_define
class ForecastScoreIntervalHitsType0:
    """Whether the outcome fell inside the central intervals (numeric)

    Attributes:
        p05p95 (bool | Unset):
        p25p75 (bool | Unset):
    """

    p05p95: bool | Unset = UNSET
    p25p75: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        p05p95 = self.p05p95

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
        p05p95 = d.pop("p05p95", UNSET)

        p25p75 = d.pop("p25p75", UNSET)

        forecast_score_interval_hits_type_0 = cls(
            p05p95=p05p95,
            p25p75=p25p75,
        )

        forecast_score_interval_hits_type_0.additional_properties = d
        return forecast_score_interval_hits_type_0

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
