from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.forecast_history_step import ForecastHistoryStep
from ..models.forecast_history_type import ForecastHistoryType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.forecast_history_series_item import ForecastHistorySeriesItem


T = TypeVar("T", bound="ForecastHistory")


@_attrs_define
class ForecastHistory:
    """Standing forecast per day for each forecaster and the ensemble, derived from immutable forecasts

    Attributes:
        question_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        type_ (ForecastHistoryType):
        step (ForecastHistoryStep):
        series (list[ForecastHistorySeriesItem]):
        from_ (datetime.date | Unset):
        to (datetime.date | Unset):
    """

    question_id: str
    type_: ForecastHistoryType
    step: ForecastHistoryStep
    series: list[ForecastHistorySeriesItem]
    from_: datetime.date | Unset = UNSET
    to: datetime.date | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        question_id = self.question_id

        type_ = self.type_.value

        step = self.step.value

        series = []
        for series_item_data in self.series:
            series_item = series_item_data.to_dict()
            series.append(series_item)

        from_: str | Unset = UNSET
        if not isinstance(self.from_, Unset):
            from_ = self.from_.isoformat()

        to: str | Unset = UNSET
        if not isinstance(self.to, Unset):
            to = self.to.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "questionId": question_id,
                "type": type_,
                "step": step,
                "series": series,
            }
        )
        if from_ is not UNSET:
            field_dict["from"] = from_
        if to is not UNSET:
            field_dict["to"] = to

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.forecast_history_series_item import ForecastHistorySeriesItem

        d = dict(src_dict)
        question_id = d.pop("questionId")

        type_ = ForecastHistoryType(d.pop("type"))

        step = ForecastHistoryStep(d.pop("step"))

        series = []
        _series = d.pop("series")
        for series_item_data in _series:
            series_item = ForecastHistorySeriesItem.from_dict(series_item_data)

            series.append(series_item)

        _from_ = d.pop("from", UNSET)
        from_: datetime.date | Unset
        if isinstance(_from_, Unset):
            from_ = UNSET
        else:
            from_ = datetime.date.fromisoformat(_from_)

        _to = d.pop("to", UNSET)
        to: datetime.date | Unset
        if isinstance(_to, Unset):
            to = UNSET
        else:
            to = datetime.date.fromisoformat(_to)

        forecast_history = cls(
            question_id=question_id,
            type_=type_,
            step=step,
            series=series,
            from_=from_,
            to=to,
        )

        forecast_history.additional_properties = d
        return forecast_history

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
