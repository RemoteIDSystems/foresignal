from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ScorecardEntryBiasType0")


@_attrs_define
class ScorecardEntryBiasType0:
    """Bias monitor

    Attributes:
        mean_forecast_minus_observed (float):
        hedged_share (float): Share of forecasts in [0.4, 0.6]
        mean_forecast_on_no (float | None | Unset): Mean forecast on questions that resolved no
    """

    mean_forecast_minus_observed: float
    hedged_share: float
    mean_forecast_on_no: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        mean_forecast_minus_observed = self.mean_forecast_minus_observed

        hedged_share = self.hedged_share

        mean_forecast_on_no: float | None | Unset
        if isinstance(self.mean_forecast_on_no, Unset):
            mean_forecast_on_no = UNSET
        else:
            mean_forecast_on_no = self.mean_forecast_on_no

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "meanForecastMinusObserved": mean_forecast_minus_observed,
                "hedgedShare": hedged_share,
            }
        )
        if mean_forecast_on_no is not UNSET:
            field_dict["meanForecastOnNo"] = mean_forecast_on_no

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        mean_forecast_minus_observed = d.pop("meanForecastMinusObserved")

        hedged_share = d.pop("hedgedShare")

        def _parse_mean_forecast_on_no(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        mean_forecast_on_no = _parse_mean_forecast_on_no(
            d.pop("meanForecastOnNo", UNSET)
        )

        scorecard_entry_bias_type_0 = cls(
            mean_forecast_minus_observed=mean_forecast_minus_observed,
            hedged_share=hedged_share,
            mean_forecast_on_no=mean_forecast_on_no,
        )

        scorecard_entry_bias_type_0.additional_properties = d
        return scorecard_entry_bias_type_0

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
