from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.stats_series_backend import StatsSeriesBackend
from ..models.stats_series_group_by import StatsSeriesGroupBy
from ..models.stats_series_measure import StatsSeriesMeasure
from ..models.stats_series_time_field import StatsSeriesTimeField
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.stats_series_series_item import StatsSeriesSeriesItem


T = TypeVar("T", bound="StatsSeries")


@_attrs_define
class StatsSeries:
    """Time series of activity measures (038)

    Attributes:
        from_ (datetime.datetime):
        to (datetime.datetime):
        bucket (str):
        measure (StatsSeriesMeasure):
        backend (StatsSeriesBackend):
        series (list[StatsSeriesSeriesItem]):
        group_by (StatsSeriesGroupBy | Unset):
        time_field (StatsSeriesTimeField | Unset):
        retained_from (datetime.datetime | None | Unset):
    """

    from_: datetime.datetime
    to: datetime.datetime
    bucket: str
    measure: StatsSeriesMeasure
    backend: StatsSeriesBackend
    series: list[StatsSeriesSeriesItem]
    group_by: StatsSeriesGroupBy | Unset = UNSET
    time_field: StatsSeriesTimeField | Unset = UNSET
    retained_from: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from_ = self.from_.isoformat()

        to = self.to.isoformat()

        bucket = self.bucket

        measure = self.measure.value

        backend = self.backend.value

        series = []
        for series_item_data in self.series:
            series_item = series_item_data.to_dict()
            series.append(series_item)

        group_by: str | Unset = UNSET
        if not isinstance(self.group_by, Unset):
            group_by = self.group_by.value

        time_field: str | Unset = UNSET
        if not isinstance(self.time_field, Unset):
            time_field = self.time_field.value

        retained_from: None | str | Unset
        if isinstance(self.retained_from, Unset):
            retained_from = UNSET
        elif isinstance(self.retained_from, datetime.datetime):
            retained_from = self.retained_from.isoformat()
        else:
            retained_from = self.retained_from

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "from": from_,
                "to": to,
                "bucket": bucket,
                "measure": measure,
                "backend": backend,
                "series": series,
            }
        )
        if group_by is not UNSET:
            field_dict["groupBy"] = group_by
        if time_field is not UNSET:
            field_dict["timeField"] = time_field
        if retained_from is not UNSET:
            field_dict["retainedFrom"] = retained_from

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.stats_series_series_item import StatsSeriesSeriesItem

        d = dict(src_dict)
        from_ = datetime.datetime.fromisoformat(d.pop("from"))

        to = datetime.datetime.fromisoformat(d.pop("to"))

        bucket = d.pop("bucket")

        measure = StatsSeriesMeasure(d.pop("measure"))

        backend = StatsSeriesBackend(d.pop("backend"))

        series = []
        _series = d.pop("series")
        for series_item_data in _series:
            series_item = StatsSeriesSeriesItem.from_dict(series_item_data)

            series.append(series_item)

        _group_by = d.pop("groupBy", UNSET)
        group_by: StatsSeriesGroupBy | Unset
        if isinstance(_group_by, Unset):
            group_by = UNSET
        else:
            group_by = StatsSeriesGroupBy(_group_by)

        _time_field = d.pop("timeField", UNSET)
        time_field: StatsSeriesTimeField | Unset
        if isinstance(_time_field, Unset):
            time_field = UNSET
        else:
            time_field = StatsSeriesTimeField(_time_field)

        def _parse_retained_from(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                retained_from_type_0 = datetime.datetime.fromisoformat(data)

                return retained_from_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        retained_from = _parse_retained_from(d.pop("retainedFrom", UNSET))

        stats_series = cls(
            from_=from_,
            to=to,
            bucket=bucket,
            measure=measure,
            backend=backend,
            series=series,
            group_by=group_by,
            time_field=time_field,
            retained_from=retained_from,
        )

        stats_series.additional_properties = d
        return stats_series

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
