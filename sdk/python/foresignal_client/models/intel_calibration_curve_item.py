from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="IntelCalibrationCurveItem")


@_attrs_define
class IntelCalibrationCurveItem:
    """
    Attributes:
        forecast (float | Unset):
        calibrated (float | Unset):
    """

    forecast: float | Unset = UNSET
    calibrated: float | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        forecast = self.forecast

        calibrated = self.calibrated

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if forecast is not UNSET:
            field_dict["forecast"] = forecast
        if calibrated is not UNSET:
            field_dict["calibrated"] = calibrated

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        forecast = d.pop("forecast", UNSET)

        calibrated = d.pop("calibrated", UNSET)

        intel_calibration_curve_item = cls(
            forecast=forecast,
            calibrated=calibrated,
        )

        intel_calibration_curve_item.additional_properties = d
        return intel_calibration_curve_item

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
