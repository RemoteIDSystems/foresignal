from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.series_catalog_entry_egress import SeriesCatalogEntryEgress
from ..models.series_catalog_entry_family import SeriesCatalogEntryFamily
from ..models.series_catalog_entry_transform import SeriesCatalogEntryTransform
from ..models.series_info_frequency import SeriesInfoFrequency
from ..models.series_info_status_type_0 import SeriesInfoStatusType0
from ..types import UNSET, Unset

T = TypeVar("T", bound="SeriesCatalogEntry")


@_attrs_define
class SeriesCatalogEntry:
    """A /v1/series catalogue item: 093's SeriesInfo plus the series metadata event studies need (also carried in the
    series-latest payload) (117)

        Attributes:
            source_id (str):  Example: gie-agsi.
            series (str):  Example: agsi:eu.
            series_name (str):
            unit (str): UCUM unit Example: %.
            frequency (SeriesInfoFrequency):
            family (SeriesCatalogEntryFamily):
            transform (SeriesCatalogEntryTransform): Transform hint for analysis
            release_lag_days (float): Typical delay between period and publication; the version's ingest time is its knownAt
            egress (SeriesCatalogEntryEgress): The egress in force for the source (062; the operator override where the
                descriptor allows one); LOCAL_ONLY raw values are served only to callers allowed local egress, so such a series
                is absent from the catalog for other callers
            dataset (None | str | Unset):
            area (None | str | Unset):
            latest_period (None | str | Unset):
            latest_value (float | None | Unset):
            status (None | SeriesInfoStatusType0 | Unset):
            region_code (None | str | Unset): 040 region code
            topic_id (None | str | Unset): Attention topic id
            attribution (None | str | Unset): The credit line the provider asks for: the FRED notice plus the series' source
                line for fred-series, the indicator's source organization for worldbank-indicators, else the source's
                attribution (117)
    """

    source_id: str
    series: str
    series_name: str
    unit: str
    frequency: SeriesInfoFrequency
    family: SeriesCatalogEntryFamily
    transform: SeriesCatalogEntryTransform
    release_lag_days: float
    egress: SeriesCatalogEntryEgress
    dataset: None | str | Unset = UNSET
    area: None | str | Unset = UNSET
    latest_period: None | str | Unset = UNSET
    latest_value: float | None | Unset = UNSET
    status: None | SeriesInfoStatusType0 | Unset = UNSET
    region_code: None | str | Unset = UNSET
    topic_id: None | str | Unset = UNSET
    attribution: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source_id = self.source_id

        series = self.series

        series_name = self.series_name

        unit = self.unit

        frequency = self.frequency.value

        family = self.family.value

        transform = self.transform.value

        release_lag_days = self.release_lag_days

        egress = self.egress.value

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

        region_code: None | str | Unset
        if isinstance(self.region_code, Unset):
            region_code = UNSET
        else:
            region_code = self.region_code

        topic_id: None | str | Unset
        if isinstance(self.topic_id, Unset):
            topic_id = UNSET
        else:
            topic_id = self.topic_id

        attribution: None | str | Unset
        if isinstance(self.attribution, Unset):
            attribution = UNSET
        else:
            attribution = self.attribution

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sourceId": source_id,
                "series": series,
                "seriesName": series_name,
                "unit": unit,
                "frequency": frequency,
                "family": family,
                "transform": transform,
                "releaseLagDays": release_lag_days,
                "egress": egress,
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
        if region_code is not UNSET:
            field_dict["regionCode"] = region_code
        if topic_id is not UNSET:
            field_dict["topicId"] = topic_id
        if attribution is not UNSET:
            field_dict["attribution"] = attribution

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        source_id = d.pop("sourceId")

        series = d.pop("series")

        series_name = d.pop("seriesName")

        unit = d.pop("unit")

        frequency = SeriesInfoFrequency(d.pop("frequency"))

        family = SeriesCatalogEntryFamily(d.pop("family"))

        transform = SeriesCatalogEntryTransform(d.pop("transform"))

        release_lag_days = d.pop("releaseLagDays")

        egress = SeriesCatalogEntryEgress(d.pop("egress"))

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

        def _parse_region_code(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        region_code = _parse_region_code(d.pop("regionCode", UNSET))

        def _parse_topic_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        topic_id = _parse_topic_id(d.pop("topicId", UNSET))

        def _parse_attribution(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        attribution = _parse_attribution(d.pop("attribution", UNSET))

        series_catalog_entry = cls(
            source_id=source_id,
            series=series,
            series_name=series_name,
            unit=unit,
            frequency=frequency,
            family=family,
            transform=transform,
            release_lag_days=release_lag_days,
            egress=egress,
            dataset=dataset,
            area=area,
            latest_period=latest_period,
            latest_value=latest_value,
            status=status,
            region_code=region_code,
            topic_id=topic_id,
            attribution=attribution,
        )

        series_catalog_entry.additional_properties = d
        return series_catalog_entry

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
