from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.analysis_series_input_detectors_item import (
    AnalysisSeriesInputDetectorsItem,
)
from ..models.analysis_series_input_frequency import AnalysisSeriesInputFrequency
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.watch_pair import WatchPair


T = TypeVar("T", bound="AnalysisSeriesInput")


@_attrs_define
class AnalysisSeriesInput:
    """Create or replace an operator analysis-series row (upsert by `ref`)

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
        enabled (bool | Unset):  Default: True.
        detectors (list[AnalysisSeriesInputDetectorsItem] | Unset):
        frequency (AnalysisSeriesInputFrequency | Unset):
        family (None | str | Unset): Signal family from the 119 registry (`intel/signal-families.json`, operator-
            extensible), e.g. `ais-behaviour`, `aviation-military`, `aviation-ops`, `conflict`, `outage`, `internet-
            measurement`, `gnss-interference`, `nav-warning`, `attention`, `seismic`, `fire`, `weather-severe`, `cyber`,
            `markets`, `sanctions`, `health`, `space`
        region_code (None | str | Unset):
        watch_pairs (list[WatchPair] | Unset):
    """

    ref: str
    enabled: bool | Unset = True
    detectors: list[AnalysisSeriesInputDetectorsItem] | Unset = UNSET
    frequency: AnalysisSeriesInputFrequency | Unset = UNSET
    family: None | str | Unset = UNSET
    region_code: None | str | Unset = UNSET
    watch_pairs: list[WatchPair] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        ref = self.ref

        enabled = self.enabled

        detectors: list[str] | Unset = UNSET
        if not isinstance(self.detectors, Unset):
            detectors = []
            for detectors_item_data in self.detectors:
                detectors_item = detectors_item_data.value
                detectors.append(detectors_item)

        frequency: str | Unset = UNSET
        if not isinstance(self.frequency, Unset):
            frequency = self.frequency.value

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

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ref": ref,
            }
        )
        if enabled is not UNSET:
            field_dict["enabled"] = enabled
        if detectors is not UNSET:
            field_dict["detectors"] = detectors
        if frequency is not UNSET:
            field_dict["frequency"] = frequency
        if family is not UNSET:
            field_dict["family"] = family
        if region_code is not UNSET:
            field_dict["regionCode"] = region_code
        if watch_pairs is not UNSET:
            field_dict["watchPairs"] = watch_pairs

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.watch_pair import WatchPair

        d = dict(src_dict)
        ref = d.pop("ref")

        enabled = d.pop("enabled", UNSET)

        _detectors = d.pop("detectors", UNSET)
        detectors: list[AnalysisSeriesInputDetectorsItem] | Unset = UNSET
        if _detectors is not UNSET:
            detectors = []
            for detectors_item_data in _detectors:
                detectors_item = AnalysisSeriesInputDetectorsItem(detectors_item_data)

                detectors.append(detectors_item)

        _frequency = d.pop("frequency", UNSET)
        frequency: AnalysisSeriesInputFrequency | Unset
        if isinstance(_frequency, Unset):
            frequency = UNSET
        else:
            frequency = AnalysisSeriesInputFrequency(_frequency)

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

        analysis_series_input = cls(
            ref=ref,
            enabled=enabled,
            detectors=detectors,
            frequency=frequency,
            family=family,
            region_code=region_code,
            watch_pairs=watch_pairs,
        )

        analysis_series_input.additional_properties = d
        return analysis_series_input

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
