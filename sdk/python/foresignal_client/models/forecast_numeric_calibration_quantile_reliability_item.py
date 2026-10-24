from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ForecastNumericCalibrationQuantileReliabilityItem")


@_attrs_define
class ForecastNumericCalibrationQuantileReliabilityItem:
    """
    Attributes:
        level (float | Unset):
        quantile (str | Unset):  Example: p05.
        nominal (float | Unset):
        empirical (float | Unset):
        n (int | Unset):
    """

    level: float | Unset = UNSET
    quantile: str | Unset = UNSET
    nominal: float | Unset = UNSET
    empirical: float | Unset = UNSET
    n: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        level = self.level

        quantile = self.quantile

        nominal = self.nominal

        empirical = self.empirical

        n = self.n

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if level is not UNSET:
            field_dict["level"] = level
        if quantile is not UNSET:
            field_dict["quantile"] = quantile
        if nominal is not UNSET:
            field_dict["nominal"] = nominal
        if empirical is not UNSET:
            field_dict["empirical"] = empirical
        if n is not UNSET:
            field_dict["n"] = n

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        level = d.pop("level", UNSET)

        quantile = d.pop("quantile", UNSET)

        nominal = d.pop("nominal", UNSET)

        empirical = d.pop("empirical", UNSET)

        n = d.pop("n", UNSET)

        forecast_numeric_calibration_quantile_reliability_item = cls(
            level=level,
            quantile=quantile,
            nominal=nominal,
            empirical=empirical,
            n=n,
        )

        forecast_numeric_calibration_quantile_reliability_item.additional_properties = d
        return forecast_numeric_calibration_quantile_reliability_item

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
