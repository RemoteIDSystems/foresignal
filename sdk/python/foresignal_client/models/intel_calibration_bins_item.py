from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="IntelCalibrationBinsItem")


@_attrs_define
class IntelCalibrationBinsItem:
    """
    Attributes:
        lower (float | Unset):
        upper (float | Unset):
        n (int | Unset):
        mean_forecast (float | None | Unset):
        observed_frequency (float | None | Unset):
    """

    lower: float | Unset = UNSET
    upper: float | Unset = UNSET
    n: int | Unset = UNSET
    mean_forecast: float | None | Unset = UNSET
    observed_frequency: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        lower = self.lower

        upper = self.upper

        n = self.n

        mean_forecast: float | None | Unset
        if isinstance(self.mean_forecast, Unset):
            mean_forecast = UNSET
        else:
            mean_forecast = self.mean_forecast

        observed_frequency: float | None | Unset
        if isinstance(self.observed_frequency, Unset):
            observed_frequency = UNSET
        else:
            observed_frequency = self.observed_frequency

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if lower is not UNSET:
            field_dict["lower"] = lower
        if upper is not UNSET:
            field_dict["upper"] = upper
        if n is not UNSET:
            field_dict["n"] = n
        if mean_forecast is not UNSET:
            field_dict["meanForecast"] = mean_forecast
        if observed_frequency is not UNSET:
            field_dict["observedFrequency"] = observed_frequency

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        lower = d.pop("lower", UNSET)

        upper = d.pop("upper", UNSET)

        n = d.pop("n", UNSET)

        def _parse_mean_forecast(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        mean_forecast = _parse_mean_forecast(d.pop("meanForecast", UNSET))

        def _parse_observed_frequency(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        observed_frequency = _parse_observed_frequency(
            d.pop("observedFrequency", UNSET)
        )

        intel_calibration_bins_item = cls(
            lower=lower,
            upper=upper,
            n=n,
            mean_forecast=mean_forecast,
            observed_frequency=observed_frequency,
        )

        intel_calibration_bins_item.additional_properties = d
        return intel_calibration_bins_item

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
