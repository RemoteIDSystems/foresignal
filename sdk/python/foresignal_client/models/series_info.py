from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.series_info_frequency import SeriesInfoFrequency
from ..models.series_info_status_type_0 import SeriesInfoStatusType0
from ..types import UNSET, Unset

T = TypeVar("T", bound="SeriesInfo")


@_attrs_define
class SeriesInfo:
    """Catalogue entry for one series, from its `series-latest` record (093)

    Attributes:
        source_id (str):  Example: gie-agsi.
        series (str):  Example: agsi:eu.
        series_name (str):
        unit (str): UCUM unit Example: %.
        frequency (SeriesInfoFrequency):
        dataset (None | str | Unset):
        area (None | str | Unset):
        latest_period (None | str | Unset):
        latest_value (float | None | Unset):
        status (None | SeriesInfoStatusType0 | Unset):
    """

    source_id: str
    series: str
    series_name: str
    unit: str
    frequency: SeriesInfoFrequency
    dataset: None | str | Unset = UNSET
    area: None | str | Unset = UNSET
    latest_period: None | str | Unset = UNSET
    latest_value: float | None | Unset = UNSET
    status: None | SeriesInfoStatusType0 | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source_id = self.source_id

        series = self.series

        series_name = self.series_name

        unit = self.unit

        frequency = self.frequency.value

        dataset: None | str | Unset
        if isinstance(self.dataset, Unset):
            dataset = UNSET
        else:
            dataset = self.dataset

        area: None | str | Unset
        if isinstance(self.area, Unset):
            area = UNSET
        else:
            area = self.area

        latest_period: None | str | Unset
        if isinstance(self.latest_period, Unset):
            latest_period = UNSET
        else:
            latest_period = self.latest_period

        latest_value: float | None | Unset
        if isinstance(self.latest_value, Unset):
            latest_value = UNSET
        else:
            latest_value = self.latest_value

        status: None | str | Unset
        if isinstance(self.status, Unset):
            status = UNSET
        elif isinstance(self.status, SeriesInfoStatusType0):
            status = self.status.value
        else:
            status = self.status

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sourceId": source_id,
                "series": series,
                "seriesName": series_name,
                "unit": unit,
                "frequency": frequency,
            }
        )
        if dataset is not UNSET:
            field_dict["dataset"] = dataset
        if area is not UNSET:
            field_dict["area"] = area
        if latest_period is not UNSET:
            field_dict["latestPeriod"] = latest_period
        if latest_value is not UNSET:
            field_dict["latestValue"] = latest_value
        if status is not UNSET:
            field_dict["status"] = status

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        source_id = d.pop("sourceId")

        series = d.pop("series")

        series_name = d.pop("seriesName")

        unit = d.pop("unit")

        frequency = SeriesInfoFrequency(d.pop("frequency"))

        def _parse_dataset(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        dataset = _parse_dataset(d.pop("dataset", UNSET))

        def _parse_area(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        area = _parse_area(d.pop("area", UNSET))

        def _parse_latest_period(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        latest_period = _parse_latest_period(d.pop("latestPeriod", UNSET))

        def _parse_latest_value(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        latest_value = _parse_latest_value(d.pop("latestValue", UNSET))

        def _parse_status(data: object) -> None | SeriesInfoStatusType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                status_type_0 = SeriesInfoStatusType0(data)

                return status_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SeriesInfoStatusType0 | Unset, data)

        status = _parse_status(d.pop("status", UNSET))

        series_info = cls(
            source_id=source_id,
            series=series,
            series_name=series_name,
            unit=unit,
            frequency=frequency,
            dataset=dataset,
            area=area,
            latest_period=latest_period,
            latest_value=latest_value,
            status=status,
        )

        series_info.additional_properties = d
        return series_info

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
