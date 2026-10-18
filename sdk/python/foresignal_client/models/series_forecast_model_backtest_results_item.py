from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.series_forecast_model_backtest_results_item_method import (
    SeriesForecastModelBacktestResultsItemMethod,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="SeriesForecastModelBacktestResultsItem")


@_attrs_define
class SeriesForecastModelBacktestResultsItem:
    """Score of one method at one horizon

    Attributes:
        method (SeriesForecastModelBacktestResultsItemMethod):
        horizon (int):
        mean_pinball_loss (float):
        interval_coverage_p05p95 (float | None | Unset):
    """

    method: SeriesForecastModelBacktestResultsItemMethod
    horizon: int
    mean_pinball_loss: float
    interval_coverage_p05p95: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        method = self.method.value

        horizon = self.horizon

        mean_pinball_loss = self.mean_pinball_loss

        interval_coverage_p05p95: float | None | Unset
        if isinstance(self.interval_coverage_p05p95, Unset):
            interval_coverage_p05p95 = UNSET
        else:
            interval_coverage_p05p95 = self.interval_coverage_p05p95

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "method": method,
                "horizon": horizon,
                "meanPinballLoss": mean_pinball_loss,
            }
        )
        if interval_coverage_p05p95 is not UNSET:
            field_dict["intervalCoverageP05P95"] = interval_coverage_p05p95

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        method = SeriesForecastModelBacktestResultsItemMethod(d.pop("method"))

        horizon = d.pop("horizon")

        mean_pinball_loss = d.pop("meanPinballLoss")

        def _parse_interval_coverage_p05p95(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        interval_coverage_p05p95 = _parse_interval_coverage_p05p95(
            d.pop("intervalCoverageP05P95", UNSET)
        )

        series_forecast_model_backtest_results_item = cls(
            method=method,
            horizon=horizon,
            mean_pinball_loss=mean_pinball_loss,
            interval_coverage_p05p95=interval_coverage_p05p95,
        )

        series_forecast_model_backtest_results_item.additional_properties = d
        return series_forecast_model_backtest_results_item

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
