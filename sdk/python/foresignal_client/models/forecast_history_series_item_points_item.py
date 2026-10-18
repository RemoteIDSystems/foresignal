from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.forecast_history_series_item_points_item_distribution_type_0 import (
        ForecastHistorySeriesItemPointsItemDistributionType0,
    )
    from ..models.quantiles import Quantiles


T = TypeVar("T", bound="ForecastHistorySeriesItemPointsItem")


@_attrs_define
class ForecastHistorySeriesItemPointsItem:
    """Standing forecast on a day

    Attributes:
        date (datetime.date):
        forecast_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        probability (float | None | Unset): Binary questions
        distribution (ForecastHistorySeriesItemPointsItemDistributionType0 | None | Unset): Multiple/ordered: outcome
            label → probability, summing to 1 ± 1e-6
        quantiles (None | Quantiles | Unset): Numeric: p05, p10, p25, p50, p75, p90, p95, monotone
    """

    date: datetime.date
    forecast_id: str
    probability: float | None | Unset = UNSET
    distribution: (
        ForecastHistorySeriesItemPointsItemDistributionType0 | None | Unset
    ) = UNSET
    quantiles: None | Quantiles | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.forecast_history_series_item_points_item_distribution_type_0 import (
            ForecastHistorySeriesItemPointsItemDistributionType0,
        )
        from ..models.quantiles import Quantiles

        date = self.date.isoformat()

        forecast_id = self.forecast_id

        probability: float | None | Unset
        if isinstance(self.probability, Unset):
            probability = UNSET
        else:
            probability = self.probability

        distribution: dict[str, Any] | None | Unset
        if isinstance(self.distribution, Unset):
            distribution = UNSET
        elif isinstance(
            self.distribution, ForecastHistorySeriesItemPointsItemDistributionType0
        ):
            distribution = self.distribution.to_dict()
        else:
            distribution = self.distribution

        quantiles: dict[str, Any] | None | Unset
        if isinstance(self.quantiles, Unset):
            quantiles = UNSET
        elif isinstance(self.quantiles, Quantiles):
            quantiles = self.quantiles.to_dict()
        else:
            quantiles = self.quantiles

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "date": date,
                "forecastId": forecast_id,
            }
        )
        if probability is not UNSET:
            field_dict["probability"] = probability
        if distribution is not UNSET:
            field_dict["distribution"] = distribution
        if quantiles is not UNSET:
            field_dict["quantiles"] = quantiles

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.forecast_history_series_item_points_item_distribution_type_0 import (
            ForecastHistorySeriesItemPointsItemDistributionType0,
        )
        from ..models.quantiles import Quantiles

        d = dict(src_dict)
        date = datetime.date.fromisoformat(d.pop("date"))

        forecast_id = d.pop("forecastId")

        def _parse_probability(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        probability = _parse_probability(d.pop("probability", UNSET))

        def _parse_distribution(
            data: object,
        ) -> ForecastHistorySeriesItemPointsItemDistributionType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                distribution_type_0 = (
                    ForecastHistorySeriesItemPointsItemDistributionType0.from_dict(data)
                )

                return distribution_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                ForecastHistorySeriesItemPointsItemDistributionType0 | None | Unset,
                data,
            )

        distribution = _parse_distribution(d.pop("distribution", UNSET))

        def _parse_quantiles(data: object) -> None | Quantiles | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                quantiles_type_0 = Quantiles.from_dict(data)

                return quantiles_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Quantiles | Unset, data)

        quantiles = _parse_quantiles(d.pop("quantiles", UNSET))

        forecast_history_series_item_points_item = cls(
            date=date,
            forecast_id=forecast_id,
            probability=probability,
            distribution=distribution,
            quantiles=quantiles,
        )

        forecast_history_series_item_points_item.additional_properties = d
        return forecast_history_series_item_points_item

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
