from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.forecast_history_series_item_points_item import (
        ForecastHistorySeriesItemPointsItem,
    )


T = TypeVar("T", bound="ForecastHistorySeriesItem")


@_attrs_define
class ForecastHistorySeriesItem:
    """One forecaster's standing forecast per step

    Attributes:
        forecaster_id (str):
        points (list[ForecastHistorySeriesItemPointsItem]):
    """

    forecaster_id: str
    points: list[ForecastHistorySeriesItemPointsItem]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        forecaster_id = self.forecaster_id

        points = []
        for points_item_data in self.points:
            points_item = points_item_data.to_dict()
            points.append(points_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "forecasterId": forecaster_id,
                "points": points,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.forecast_history_series_item_points_item import (
            ForecastHistorySeriesItemPointsItem,
        )

        d = dict(src_dict)
        forecaster_id = d.pop("forecasterId")

        points = []
        _points = d.pop("points")
        for points_item_data in _points:
            points_item = ForecastHistorySeriesItemPointsItem.from_dict(
                points_item_data
            )

            points.append(points_item)

        forecast_history_series_item = cls(
            forecaster_id=forecaster_id,
            points=points,
        )

        forecast_history_series_item.additional_properties = d
        return forecast_history_series_item

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
