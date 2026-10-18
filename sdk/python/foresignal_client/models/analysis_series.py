from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.analysis_series_detectors_item import AnalysisSeriesDetectorsItem
from ..models.analysis_series_frequency import AnalysisSeriesFrequency
from ..models.analysis_series_origin import AnalysisSeriesOrigin
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.watch_pair import WatchPair


T = TypeVar("T", bound="AnalysisSeries")


@_attrs_define
class AnalysisSeries:
    """A registered analysis series that detectors run on

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
        enabled (bool):  Default: True.
        detectors (list[AnalysisSeriesDetectorsItem]):
        frequency (AnalysisSeriesFrequency):
        origin (AnalysisSeriesOrigin): `auto` rows come from nightly auto-registration; `operator` rows are never
            removed automatically
        family (None | str | Unset): Signal family from the 119 registry (`intel/signal-families.json`, operator-
            extensible), e.g. `ais-behaviour`, `aviation-military`, `aviation-ops`, `conflict`, `outage`, `internet-
            measurement`, `gnss-interference`, `nav-warning`, `attention`, `seismic`, `fire`, `weather-severe`, `cyber`,
            `markets`, `sanctions`, `health`, `space`
        region_code (None | str | Unset):
        watch_pairs (list[WatchPair] | Unset):
        created_at (datetime.datetime | Unset):
        last_run_at (datetime.datetime | None | Unset):
        last_error (None | str | Unset):
    """

    ref: str
    detectors: list[AnalysisSeriesDetectorsItem]
    frequency: AnalysisSeriesFrequency
    origin: AnalysisSeriesOrigin
    enabled: bool = True
    family: None | str | Unset = UNSET
    region_code: None | str | Unset = UNSET
    watch_pairs: list[WatchPair] | Unset = UNSET
    created_at: datetime.datetime | Unset = UNSET
    last_run_at: datetime.datetime | None | Unset = UNSET
    last_error: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        ref = self.ref

        enabled = self.enabled

        detectors = []
        for detectors_item_data in self.detectors:
            detectors_item = detectors_item_data.value
            detectors.append(detectors_item)

        frequency = self.frequency.value

        origin = self.origin.value

        family: None | str | Unset
        if isinstance(self.family, Unset):
            family = UNSET
        else:
            family = self.family

        region_code: None | str | Unset
        if isinstance(self.region_code, Unset):
            region_code = UNSET
        else:
            region_code = self.region_code

        watch_pairs: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.watch_pairs, Unset):
            watch_pairs = []
            for watch_pairs_item_data in self.watch_pairs:
                watch_pairs_item = watch_pairs_item_data.to_dict()
                watch_pairs.append(watch_pairs_item)

        created_at: str | Unset = UNSET
        if not isinstance(self.created_at, Unset):
            created_at = self.created_at.isoformat()

        last_run_at: None | str | Unset
        if isinstance(self.last_run_at, Unset):
            last_run_at = UNSET
        elif isinstance(self.last_run_at, datetime.datetime):
            last_run_at = self.last_run_at.isoformat()
        else:
            last_run_at = self.last_run_at

        last_error: None | str | Unset
        if isinstance(self.last_error, Unset):
            last_error = UNSET
        else:
            last_error = self.last_error

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ref": ref,
                "enabled": enabled,
                "detectors": detectors,
                "frequency": frequency,
                "origin": origin,
            }
        )
        if family is not UNSET:
            field_dict["family"] = family
        if region_code is not UNSET:
            field_dict["regionCode"] = region_code
        if watch_pairs is not UNSET:
            field_dict["watchPairs"] = watch_pairs
        if created_at is not UNSET:
            field_dict["createdAt"] = created_at
        if last_run_at is not UNSET:
            field_dict["lastRunAt"] = last_run_at
        if last_error is not UNSET:
            field_dict["lastError"] = last_error

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.watch_pair import WatchPair

        d = dict(src_dict)
        ref = d.pop("ref")

        enabled = d.pop("enabled")

        detectors = []
        _detectors = d.pop("detectors")
        for detectors_item_data in _detectors:
            detectors_item = AnalysisSeriesDetectorsItem(detectors_item_data)

            detectors.append(detectors_item)

        frequency = AnalysisSeriesFrequency(d.pop("frequency"))

        origin = AnalysisSeriesOrigin(d.pop("origin"))

        def _parse_family(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        family = _parse_family(d.pop("family", UNSET))

        def _parse_region_code(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        region_code = _parse_region_code(d.pop("regionCode", UNSET))

        _watch_pairs = d.pop("watchPairs", UNSET)
        watch_pairs: list[WatchPair] | Unset = UNSET
        if _watch_pairs is not UNSET:
            watch_pairs = []
            for watch_pairs_item_data in _watch_pairs:
                watch_pairs_item = WatchPair.from_dict(watch_pairs_item_data)

                watch_pairs.append(watch_pairs_item)

        _created_at = d.pop("createdAt", UNSET)
        created_at: datetime.datetime | Unset
        if isinstance(_created_at, Unset):
            created_at = UNSET
        else:
            created_at = datetime.datetime.fromisoformat(_created_at)

        def _parse_last_run_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_run_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_run_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_run_at = _parse_last_run_at(d.pop("lastRunAt", UNSET))

        def _parse_last_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        last_error = _parse_last_error(d.pop("lastError", UNSET))

        analysis_series = cls(
            ref=ref,
            enabled=enabled,
            detectors=detectors,
            frequency=frequency,
            origin=origin,
            family=family,
            region_code=region_code,
            watch_pairs=watch_pairs,
            created_at=created_at,
            last_run_at=last_run_at,
            last_error=last_error,
        )

        analysis_series.additional_properties = d
        return analysis_series

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
