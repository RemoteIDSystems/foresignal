from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ForecastNumericCalibrationPitBinsItem")


@_attrs_define
class ForecastNumericCalibrationPitBinsItem:
    """
    Attributes:
        lower (float | Unset):
        upper (float | Unset):
        count (int | Unset):
        frequency (float | None | Unset):
        expected (float | Unset): The bin width; a calibrated forecaster's frequency is near it
    """

    lower: float | Unset = UNSET
    upper: float | Unset = UNSET
    count: int | Unset = UNSET
    frequency: float | None | Unset = UNSET
    expected: float | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        lower = self.lower

        upper = self.upper

        count = self.count

        frequency: float | None | Unset
        if isinstance(self.frequency, Unset):
            frequency = UNSET
        else:
            frequency = self.frequency

        expected = self.expected

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if lower is not UNSET:
            field_dict["lower"] = lower
        if upper is not UNSET:
            field_dict["upper"] = upper
        if count is not UNSET:
            field_dict["count"] = count
        if frequency is not UNSET:
            field_dict["frequency"] = frequency
        if expected is not UNSET:
            field_dict["expected"] = expected

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        lower = d.pop("lower", UNSET)

        upper = d.pop("upper", UNSET)

        count = d.pop("count", UNSET)

        def _parse_frequency(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        frequency = _parse_frequency(d.pop("frequency", UNSET))

        expected = d.pop("expected", UNSET)

        forecast_numeric_calibration_pit_bins_item = cls(
            lower=lower,
            upper=upper,
            count=count,
            frequency=frequency,
            expected=expected,
        )

        forecast_numeric_calibration_pit_bins_item.additional_properties = d
        return forecast_numeric_calibration_pit_bins_item

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
