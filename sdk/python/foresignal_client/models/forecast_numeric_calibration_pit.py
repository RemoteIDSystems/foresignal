from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.forecast_numeric_calibration_pit_bins_item import (
        ForecastNumericCalibrationPitBinsItem,
    )


T = TypeVar("T", bound="ForecastNumericCalibrationPit")


@_attrs_define
class ForecastNumericCalibrationPit:
    """PIT histogram over the required-quantile bins

    Attributes:
        n (int | Unset):
        bins (list[ForecastNumericCalibrationPitBinsItem] | Unset):
    """

    n: int | Unset = UNSET
    bins: list[ForecastNumericCalibrationPitBinsItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        n = self.n

        bins: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.bins, Unset):
            bins = []
            for bins_item_data in self.bins:
                bins_item = bins_item_data.to_dict()
                bins.append(bins_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if n is not UNSET:
            field_dict["n"] = n
        if bins is not UNSET:
            field_dict["bins"] = bins

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.forecast_numeric_calibration_pit_bins_item import (
            ForecastNumericCalibrationPitBinsItem,
        )

        d = dict(src_dict)
        n = d.pop("n", UNSET)

        _bins = d.pop("bins", UNSET)
        bins: list[ForecastNumericCalibrationPitBinsItem] | Unset = UNSET
        if _bins is not UNSET:
            bins = []
            for bins_item_data in _bins:
                bins_item = ForecastNumericCalibrationPitBinsItem.from_dict(
                    bins_item_data
                )

                bins.append(bins_item)

        forecast_numeric_calibration_pit = cls(
            n=n,
            bins=bins,
        )

        forecast_numeric_calibration_pit.additional_properties = d
        return forecast_numeric_calibration_pit

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
