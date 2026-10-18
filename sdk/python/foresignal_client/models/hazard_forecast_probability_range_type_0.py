from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="HazardForecastProbabilityRangeType0")


@_attrs_define
class HazardForecastProbabilityRangeType0:
    """Published range for a category

    Attributes:
        low (float):
        high (float | None | Unset):
    """

    low: float
    high: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        low = self.low

        high: float | None | Unset
        if isinstance(self.high, Unset):
            high = UNSET
        else:
            high = self.high

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "low": low,
            }
        )
        if high is not UNSET:
            field_dict["high"] = high

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        low = d.pop("low")

        def _parse_high(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        high = _parse_high(d.pop("high", UNSET))

        hazard_forecast_probability_range_type_0 = cls(
            low=low,
            high=high,
        )

        hazard_forecast_probability_range_type_0.additional_properties = d
        return hazard_forecast_probability_range_type_0

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
