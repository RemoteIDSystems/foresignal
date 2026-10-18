from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="ForecastCalibrationCurveItem")


@_attrs_define
class ForecastCalibrationCurveItem:
    """Point of the recalibrated (PAV) curve

    Attributes:
        forecast (float):
        calibrated (float):
    """

    forecast: float
    calibrated: float
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        forecast = self.forecast

        calibrated = self.calibrated

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "forecast": forecast,
                "calibrated": calibrated,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        forecast = d.pop("forecast")

        calibrated = d.pop("calibrated")

        forecast_calibration_curve_item = cls(
            forecast=forecast,
            calibrated=calibrated,
        )

        forecast_calibration_curve_item.additional_properties = d
        return forecast_calibration_curve_item

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
