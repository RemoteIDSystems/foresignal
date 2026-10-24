from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ForecastNumericCalibrationSummary")


@_attrs_define
class ForecastNumericCalibrationSummary:
    """
    Attributes:
        mean_absolute_coverage_error (float | None | Unset): Mean |empirical − nominal| over the stated levels
        max_coverage_deviation (float | None | Unset):
    """

    mean_absolute_coverage_error: float | None | Unset = UNSET
    max_coverage_deviation: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        mean_absolute_coverage_error: float | None | Unset
        if isinstance(self.mean_absolute_coverage_error, Unset):
            mean_absolute_coverage_error = UNSET
        else:
            mean_absolute_coverage_error = self.mean_absolute_coverage_error

        max_coverage_deviation: float | None | Unset
        if isinstance(self.max_coverage_deviation, Unset):
            max_coverage_deviation = UNSET
        else:
            max_coverage_deviation = self.max_coverage_deviation

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if mean_absolute_coverage_error is not UNSET:
            field_dict["meanAbsoluteCoverageError"] = mean_absolute_coverage_error
        if max_coverage_deviation is not UNSET:
            field_dict["maxCoverageDeviation"] = max_coverage_deviation

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_mean_absolute_coverage_error(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        mean_absolute_coverage_error = _parse_mean_absolute_coverage_error(
            d.pop("meanAbsoluteCoverageError", UNSET)
        )

        def _parse_max_coverage_deviation(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        max_coverage_deviation = _parse_max_coverage_deviation(
            d.pop("maxCoverageDeviation", UNSET)
        )

        forecast_numeric_calibration_summary = cls(
            mean_absolute_coverage_error=mean_absolute_coverage_error,
            max_coverage_deviation=max_coverage_deviation,
        )

        forecast_numeric_calibration_summary.additional_properties = d
        return forecast_numeric_calibration_summary

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
