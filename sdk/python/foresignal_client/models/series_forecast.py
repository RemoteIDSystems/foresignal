from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.series_forecast_method import SeriesForecastMethod
from ..models.series_forecast_units import SeriesForecastUnits
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.caveat import Caveat
    from ..models.series_forecast_forecasts_item import SeriesForecastForecastsItem


T = TypeVar("T", bound="SeriesForecast")


@_attrs_define
class SeriesForecast:
    """Latest method-selected quantile forecasts for one series (`series-forecast` 1.0.0)

    Attributes:
        ref (str): Series reference (Intelligence-Design §4.3): `series:{sourceId}/{seriesId}` (093/117 series;
            `series:imf-portwatch-chokepoints/{portId}` for PortWatch daily transits),
            `rollup:{hour|day}/{sourceId|*}/{kind}/{region|cell:h3}` (activity rollups; `*` is the cross-source event count
            with canonical events counted once) `rule:{ruleId}/{groupKey}` (100 anomaly rule samples) or
            `rollup:news/{topicId|situationId}/{bucket}` (133: daily news coverage in evidence groups by lean bucket left,
            center, right, unrated or state); or `social:{topic|place|entity}:{id}/{platform}/{metric}[/{language}]` (135:
            hourly social mention volume of a scope on one network, metric posts, uniqueAuthors, reposts or engagement,
            every language summed unless a language bucket is given; `social:all/{platform}/denominator[/{language}]` is the
            platform-language denominator; complete and paged collection only) Example: series:cftc-
            cot/067651.disagg.mm_net.
        method (SeriesForecastMethod):
        version (str):  Example: series-forecast 1.0.0.
        made_at (datetime.datetime):
        as_of_seq (int):
        units (SeriesForecastUnits): `standardised` for LOCAL_ONLY series when the caller may not see local values
        forecasts (list[SeriesForecastForecastsItem]):
        manifest_id (None | str | Unset):
        unit (None | str | Unset):
        question_ids (list[str] | Unset): Ledger questions opened for this series
        caveats (list[Caveat] | Unset):
    """

    ref: str
    method: SeriesForecastMethod
    version: str
    made_at: datetime.datetime
    as_of_seq: int
    units: SeriesForecastUnits
    forecasts: list[SeriesForecastForecastsItem]
    manifest_id: None | str | Unset = UNSET
    unit: None | str | Unset = UNSET
    question_ids: list[str] | Unset = UNSET
    caveats: list[Caveat] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        ref = self.ref

        method = self.method.value

        version = self.version

        made_at = self.made_at.isoformat()

        as_of_seq = self.as_of_seq

        units = self.units.value

        forecasts = []
        for forecasts_item_data in self.forecasts:
            forecasts_item = forecasts_item_data.to_dict()
            forecasts.append(forecasts_item)

        manifest_id: None | str | Unset
        if isinstance(self.manifest_id, Unset):
            manifest_id = UNSET
        else:
            manifest_id = self.manifest_id

        unit: None | str | Unset
        if isinstance(self.unit, Unset):
            unit = UNSET
        else:
            unit = self.unit

        question_ids: list[str] | Unset = UNSET
        if not isinstance(self.question_ids, Unset):
            question_ids = self.question_ids

        caveats: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.caveats, Unset):
            caveats = []
            for caveats_item_data in self.caveats:
                caveats_item = caveats_item_data.to_dict()
                caveats.append(caveats_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ref": ref,
                "method": method,
                "version": version,
                "madeAt": made_at,
                "asOfSeq": as_of_seq,
                "units": units,
                "forecasts": forecasts,
            }
        )
        if manifest_id is not UNSET:
            field_dict["manifestId"] = manifest_id
        if unit is not UNSET:
            field_dict["unit"] = unit
        if question_ids is not UNSET:
            field_dict["questionIds"] = question_ids
        if caveats is not UNSET:
            field_dict["caveats"] = caveats

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.caveat import Caveat
        from ..models.series_forecast_forecasts_item import SeriesForecastForecastsItem

        d = dict(src_dict)
        ref = d.pop("ref")

        method = SeriesForecastMethod(d.pop("method"))

        version = d.pop("version")

        made_at = datetime.datetime.fromisoformat(d.pop("madeAt"))

        as_of_seq = d.pop("asOfSeq")

        units = SeriesForecastUnits(d.pop("units"))

        forecasts = []
        _forecasts = d.pop("forecasts")
        for forecasts_item_data in _forecasts:
            forecasts_item = SeriesForecastForecastsItem.from_dict(forecasts_item_data)

            forecasts.append(forecasts_item)

        def _parse_manifest_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_id = _parse_manifest_id(d.pop("manifestId", UNSET))

        def _parse_unit(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        unit = _parse_unit(d.pop("unit", UNSET))

        question_ids = cast(list[str], d.pop("questionIds", UNSET))

        _caveats = d.pop("caveats", UNSET)
        caveats: list[Caveat] | Unset = UNSET
        if _caveats is not UNSET:
            caveats = []
            for caveats_item_data in _caveats:
                caveats_item = Caveat.from_dict(caveats_item_data)

                caveats.append(caveats_item)

        series_forecast = cls(
            ref=ref,
            method=method,
            version=version,
            made_at=made_at,
            as_of_seq=as_of_seq,
            units=units,
            forecasts=forecasts,
            manifest_id=manifest_id,
            unit=unit,
            question_ids=question_ids,
            caveats=caveats,
        )

        series_forecast.additional_properties = d
        return series_forecast

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
