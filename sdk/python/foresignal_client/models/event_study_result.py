from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.event_study_result_frequency import EventStudyResultFrequency
from ..models.event_study_result_transform import EventStudyResultTransform
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.caveat import Caveat
    from ..models.detector_ref import DetectorRef
    from ..models.event_study_result_caar import EventStudyResultCaar
    from ..models.event_study_result_events_item import EventStudyResultEventsItem
    from ..models.event_study_result_windows import EventStudyResultWindows
    from ..models.time_range import TimeRange


T = TypeVar("T", bound="EventStudyResult")


@_attrs_define
class EventStudyResult:
    """Result of an event study (the `result` of a 103 job of type `event-study`)

    Attributes:
        detector (DetectorRef):
        target (str): Series reference (Intelligence-Design §4.3): `series:{sourceId}/{seriesId}` (093/117 series;
            `series:imf-portwatch-chokepoints/{portId}` for PortWatch daily transits),
            `rollup:{hour|day}/{sourceId|*}/{kind}/{region|cell:h3}` (activity rollups; `*` is the cross-source event count
            with canonical events counted once) `rule:{ruleId}/{groupKey}` (100 anomaly rule samples) or
            `rollup:news/{topicId|situationId}/{bucket}` (133: daily news coverage in evidence groups by lean bucket left,
            center, right, unrated or state); or `social:{topic|place|entity}:{id}/{platform}/{metric}[/{language}]` (135:
            hourly social mention volume of a scope on one network, metric posts, uniqueAuthors, reposts or engagement,
            every language summed unless a language bucket is given; `social:all/{platform}/denominator[/{language}]` is the
            platform-language denominator; complete and paged collection only) Example: series:cftc-
            cot/067651.disagg.mm_net.
        frequency (EventStudyResultFrequency):
        windows (EventStudyResultWindows): Windows in buckets of the series' frequency
        n_events (int):
        events (list[EventStudyResultEventsItem]):
        caar (EventStudyResultCaar): CAAR and tests per window
        caveats (list[Caveat]): 103 caveats plus `FEW_EVENTS`, `LAGGED_PUBLICATION`, `DAILY_RESOLUTION_ONLY`
        manifest_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        definition_hash (str | Unset):
        transform (EventStudyResultTransform | Unset):
        range_ (TimeRange | Unset):
        as_of_seq (int | Unset):
        placebo_seed (str | Unset): Deterministic seed (hash of definition and range)
        n_clusters (int | Unset):
        n_excluded (int | Unset):
        values_withheld (bool | Unset):
        truncated (bool | Unset):
    """

    detector: DetectorRef
    target: str
    frequency: EventStudyResultFrequency
    windows: EventStudyResultWindows
    n_events: int
    events: list[EventStudyResultEventsItem]
    caar: EventStudyResultCaar
    caveats: list[Caveat]
    manifest_id: str
    definition_hash: str | Unset = UNSET
    transform: EventStudyResultTransform | Unset = UNSET
    range_: TimeRange | Unset = UNSET
    as_of_seq: int | Unset = UNSET
    placebo_seed: str | Unset = UNSET
    n_clusters: int | Unset = UNSET
    n_excluded: int | Unset = UNSET
    values_withheld: bool | Unset = UNSET
    truncated: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        detector = self.detector.to_dict()

        target = self.target

        frequency = self.frequency.value

        windows = self.windows.to_dict()

        n_events = self.n_events

        events = []
        for events_item_data in self.events:
            events_item = events_item_data.to_dict()
            events.append(events_item)

        caar = self.caar.to_dict()

        caveats = []
        for caveats_item_data in self.caveats:
            caveats_item = caveats_item_data.to_dict()
            caveats.append(caveats_item)

        manifest_id = self.manifest_id

        definition_hash = self.definition_hash

        transform: str | Unset = UNSET
        if not isinstance(self.transform, Unset):
            transform = self.transform.value

        range_: dict[str, Any] | Unset = UNSET
        if not isinstance(self.range_, Unset):
            range_ = self.range_.to_dict()

        as_of_seq = self.as_of_seq

        placebo_seed = self.placebo_seed

        n_clusters = self.n_clusters

        n_excluded = self.n_excluded

        values_withheld = self.values_withheld

        truncated = self.truncated

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "detector": detector,
                "target": target,
                "frequency": frequency,
                "windows": windows,
                "nEvents": n_events,
                "events": events,
                "caar": caar,
                "caveats": caveats,
                "manifestId": manifest_id,
            }
        )
        if definition_hash is not UNSET:
            field_dict["definitionHash"] = definition_hash
        if transform is not UNSET:
            field_dict["transform"] = transform
        if range_ is not UNSET:
            field_dict["range"] = range_
        if as_of_seq is not UNSET:
            field_dict["asOfSeq"] = as_of_seq
        if placebo_seed is not UNSET:
            field_dict["placeboSeed"] = placebo_seed
        if n_clusters is not UNSET:
            field_dict["nClusters"] = n_clusters
        if n_excluded is not UNSET:
            field_dict["nExcluded"] = n_excluded
        if values_withheld is not UNSET:
            field_dict["valuesWithheld"] = values_withheld
        if truncated is not UNSET:
            field_dict["truncated"] = truncated

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.caveat import Caveat
        from ..models.detector_ref import DetectorRef
        from ..models.event_study_result_caar import EventStudyResultCaar
        from ..models.event_study_result_events_item import EventStudyResultEventsItem
        from ..models.event_study_result_windows import EventStudyResultWindows
        from ..models.time_range import TimeRange

        d = dict(src_dict)
        detector = DetectorRef.from_dict(d.pop("detector"))

        target = d.pop("target")

        frequency = EventStudyResultFrequency(d.pop("frequency"))

        windows = EventStudyResultWindows.from_dict(d.pop("windows"))

        n_events = d.pop("nEvents")

        events = []
        _events = d.pop("events")
        for events_item_data in _events:
            events_item = EventStudyResultEventsItem.from_dict(events_item_data)

            events.append(events_item)

        caar = EventStudyResultCaar.from_dict(d.pop("caar"))

        caveats = []
        _caveats = d.pop("caveats")
        for caveats_item_data in _caveats:
            caveats_item = Caveat.from_dict(caveats_item_data)

            caveats.append(caveats_item)

        manifest_id = d.pop("manifestId")

        definition_hash = d.pop("definitionHash", UNSET)

        _transform = d.pop("transform", UNSET)
        transform: EventStudyResultTransform | Unset
        if isinstance(_transform, Unset):
            transform = UNSET
        else:
            transform = EventStudyResultTransform(_transform)

        _range_ = d.pop("range", UNSET)
        range_: TimeRange | Unset
        if isinstance(_range_, Unset):
            range_ = UNSET
        else:
            range_ = TimeRange.from_dict(_range_)

        as_of_seq = d.pop("asOfSeq", UNSET)

        placebo_seed = d.pop("placeboSeed", UNSET)

        n_clusters = d.pop("nClusters", UNSET)

        n_excluded = d.pop("nExcluded", UNSET)

        values_withheld = d.pop("valuesWithheld", UNSET)

        truncated = d.pop("truncated", UNSET)

        event_study_result = cls(
            detector=detector,
            target=target,
            frequency=frequency,
            windows=windows,
            n_events=n_events,
            events=events,
            caar=caar,
            caveats=caveats,
            manifest_id=manifest_id,
            definition_hash=definition_hash,
            transform=transform,
            range_=range_,
            as_of_seq=as_of_seq,
            placebo_seed=placebo_seed,
            n_clusters=n_clusters,
            n_excluded=n_excluded,
            values_withheld=values_withheld,
            truncated=truncated,
        )

        event_study_result.additional_properties = d
        return event_study_result

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
