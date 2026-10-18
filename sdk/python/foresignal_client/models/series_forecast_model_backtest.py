from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.series_forecast_model_backtest_results_item import (
        SeriesForecastModelBacktestResultsItem,
    )


T = TypeVar("T", bound="SeriesForecastModelBacktest")


@_attrs_define
class SeriesForecastModelBacktest:
    """Rolling-origin backtest (last 8 weekly origins)

    Attributes:
        origins (int):
        results (list[SeriesForecastModelBacktestResultsItem]):
        spacing (str | Unset):  Example: P7D.
        selection_horizon (int | Unset):  Example: 7.
    """

    origins: int
    results: list[SeriesForecastModelBacktestResultsItem]
    spacing: str | Unset = UNSET
    selection_horizon: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        origins = self.origins

        results = []
        for results_item_data in self.results:
            results_item = results_item_data.to_dict()
            results.append(results_item)

        spacing = self.spacing

        selection_horizon = self.selection_horizon

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "origins": origins,
                "results": results,
            }
        )
        if spacing is not UNSET:
            field_dict["spacing"] = spacing
        if selection_horizon is not UNSET:
            field_dict["selectionHorizon"] = selection_horizon

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.series_forecast_model_backtest_results_item import (
            SeriesForecastModelBacktestResultsItem,
        )

        d = dict(src_dict)
        origins = d.pop("origins")

        results = []
        _results = d.pop("results")
        for results_item_data in _results:
            results_item = SeriesForecastModelBacktestResultsItem.from_dict(
                results_item_data
            )

            results.append(results_item)

        spacing = d.pop("spacing", UNSET)

        selection_horizon = d.pop("selectionHorizon", UNSET)

        series_forecast_model_backtest = cls(
            origins=origins,
            results=results,
            spacing=spacing,
            selection_horizon=selection_horizon,
        )

        series_forecast_model_backtest.additional_properties = d
        return series_forecast_model_backtest

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
