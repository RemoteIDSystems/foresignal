from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.series_forecast_forecasts_item_horizon import (
    SeriesForecastForecastsItemHorizon,
)

if TYPE_CHECKING:
    from ..models.quantiles import Quantiles
    from ..models.time_range import TimeRange


T = TypeVar("T", bound="SeriesForecastForecastsItem")


@_attrs_define
class SeriesForecastForecastsItem:
    """Forecast for one horizon

    Attributes:
        horizon (SeriesForecastForecastsItemHorizon): Days ahead
        target_period (TimeRange):
        quantiles (Quantiles):
    """

    horizon: SeriesForecastForecastsItemHorizon
    target_period: TimeRange
    quantiles: Quantiles
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        horizon = self.horizon.value

        target_period = self.target_period.to_dict()

        quantiles = self.quantiles.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "horizon": horizon,
                "targetPeriod": target_period,
                "quantiles": quantiles,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.quantiles import Quantiles
        from ..models.time_range import TimeRange

        d = dict(src_dict)
        horizon = SeriesForecastForecastsItemHorizon(d.pop("horizon"))

        target_period = TimeRange.from_dict(d.pop("targetPeriod"))

        quantiles = Quantiles.from_dict(d.pop("quantiles"))

        series_forecast_forecasts_item = cls(
            horizon=horizon,
            target_period=target_period,
            quantiles=quantiles,
        )

        series_forecast_forecasts_item.additional_properties = d
        return series_forecast_forecasts_item

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
