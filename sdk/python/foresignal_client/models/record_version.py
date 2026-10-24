from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.history_tier import HistoryTier
from ..models.persist_reason import PersistReason
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry
    from ..models.observer import Observer
    from ..models.provenance import Provenance
    from ..models.quality import Quality
    from ..models.record_payload import RecordPayload
    from ..models.region_tags import RegionTags


T = TypeVar("T", bound="RecordVersion")


@_attrs_define
class RecordVersion:
    """One persisted version of a record (versions endpoint)

    Attributes:
        id (str): <sourceId>:<externalId>
        source (str):
        external_id (str):
        kind (str):
        version (int):
        seq (int):
        observed_at (datetime.datetime):
        payload (RecordPayload): Current payload `jsonb_strip_nulls(h.payload || l.kinematic)`; projected by `fields`
        ingested_at (datetime.datetime): Versions, history and asOf responses only
        persist_reason (PersistReason): Why a version was persisted (Core-Data-Model §5.2; stored codes 1–9)
        received_at (datetime.datetime):
        valid_until (datetime.datetime | None | Unset):
        tombstone (bool | Unset):
        geometry (Geometry | None | Unset):
        attribution (None | str | Unset): null when the source has no attribution line
        title (None | str | Unset): record_head.title (titleField, else title > name > callsign > place > r > hex >
            mmsi)
        persisted_observed_at (datetime.datetime | Unset): observedAt of the persisted version (observedAt may be newer
            from live updates)
        event_time (datetime.datetime | None | Unset): Discrete event time or measurement period start
        valid_from (datetime.datetime | None | Unset): Start of validity (`onset ?? effective` for alerts, scheduled
            start for road events); nullable, hashed like validUntil
        quality (None | Quality | Unset):
        live (bool | Unset): True when live state differs from the persisted version (record_live.dirty)
        qa_flags (int | Unset): Quality bitmask: 1 FUTURE_CLAMPED, 2 GEOMETRY_FIXED, 4 NULL_ISLAND, 8 TIME_IMPUTED, 16
            POSITION_STALE
        distance_km (float | Unset): Distance from `near` (sort=distance only)
        observer (list[Observer] | Unset): Only with include=observer for agent/admin tokens or when
            security.exposeObserver is on; otherwise omitted
        region (None | RegionTags | Unset): Present with include=region (040)
        entity_id (None | str | Unset): Canonical entity from the record's open link; present with `include=entity`
        provenance (Provenance | Unset):
        global_ref (str | Unset): urn:foresignal:{instanceId}:{sourceId}:{externalId}
        prev_seq (int | None | Unset): seq of the previous version of this record
        history_tier (HistoryTier | Unset): History tier: STANDARD (per-source windows, chunked deletes) or TRACK (one
            `trackWindow`, partition drops)
        batch_id (int | None | Unset): fetch_log id of the batch that wrote this version
        downsampled (bool | Unset): Seq falls in a downsampled (thinned) TRACK leaf; a `prevSeq` that no longer exists
            was removed by downsampling Default: False.
    """

    id: str
    source: str
    external_id: str
    kind: str
    version: int
    seq: int
    observed_at: datetime.datetime
    payload: RecordPayload
    ingested_at: datetime.datetime
    persist_reason: PersistReason
    received_at: datetime.datetime
    valid_until: datetime.datetime | None | Unset = UNSET
    tombstone: bool | Unset = UNSET
    geometry: Geometry | None | Unset = UNSET
    attribution: None | str | Unset = UNSET
    title: None | str | Unset = UNSET
    persisted_observed_at: datetime.datetime | Unset = UNSET
    event_time: datetime.datetime | None | Unset = UNSET
    valid_from: datetime.datetime | None | Unset = UNSET
    quality: None | Quality | Unset = UNSET
    live: bool | Unset = UNSET
    qa_flags: int | Unset = UNSET
    distance_km: float | Unset = UNSET
    observer: list[Observer] | Unset = UNSET
    region: None | RegionTags | Unset = UNSET
    entity_id: None | str | Unset = UNSET
    provenance: Provenance | Unset = UNSET
    global_ref: str | Unset = UNSET
    prev_seq: int | None | Unset = UNSET
    history_tier: HistoryTier | Unset = UNSET
    batch_id: int | None | Unset = UNSET
    downsampled: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.geometry import Geometry
        from ..models.quality import Quality
        from ..models.region_tags import RegionTags

        id = self.id

        source = self.source

        external_id = self.external_id

        kind = self.kind

        version = self.version

        seq = self.seq

        observed_at = self.observed_at.isoformat()

        payload = self.payload.to_dict()

        ingested_at = self.ingested_at.isoformat()

        persist_reason = self.persist_reason.value

        received_at = self.received_at.isoformat()

        valid_until: None | str | Unset
        if isinstance(self.valid_until, Unset):
            valid_until = UNSET
        elif isinstance(self.valid_until, datetime.datetime):
            valid_until = self.valid_until.isoformat()
        else:
            valid_until = self.valid_until

        tombstone = self.tombstone

        geometry: dict[str, Any] | None | Unset
        if isinstance(self.geometry, Unset):
            geometry = UNSET
        elif isinstance(self.geometry, Geometry):
            geometry = self.geometry.to_dict()
        else:
            geometry = self.geometry

        attribution: None | str | Unset
        if isinstance(self.attribution, Unset):
            attribution = UNSET
        else:
            attribution = self.attribution

        title: None | str | Unset
        if isinstance(self.title, Unset):
            title = UNSET
        else:
            title = self.title

        persisted_observed_at: str | Unset = UNSET
        if not isinstance(self.persisted_observed_at, Unset):
            persisted_observed_at = self.persisted_observed_at.isoformat()

        event_time: None | str | Unset
        if isinstance(self.event_time, Unset):
            event_time = UNSET
        elif isinstance(self.event_time, datetime.datetime):
            event_time = self.event_time.isoformat()
        else:
            event_time = self.event_time

        valid_from: None | str | Unset
        if isinstance(self.valid_from, Unset):
            valid_from = UNSET
        elif isinstance(self.valid_from, datetime.datetime):
            valid_from = self.valid_from.isoformat()
        else:
            valid_from = self.valid_from

        quality: dict[str, Any] | None | Unset
        if isinstance(self.quality, Unset):
            quality = UNSET
        elif isinstance(self.quality, Quality):
            quality = self.quality.to_dict()
        else:
            quality = self.quality

        live = self.live

        qa_flags = self.qa_flags

        distance_km = self.distance_km

        observer: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.observer, Unset):
            observer = []
            for observer_item_data in self.observer:
                observer_item = observer_item_data.to_dict()
                observer.append(observer_item)

        region: dict[str, Any] | None | Unset
        if isinstance(self.region, Unset):
            region = UNSET
        elif isinstance(self.region, RegionTags):
            region = self.region.to_dict()
        else:
            region = self.region

        entity_id: None | str | Unset
        if isinstance(self.entity_id, Unset):
            entity_id = UNSET
        else:
            entity_id = self.entity_id

        provenance: dict[str, Any] | Unset = UNSET
        if not isinstance(self.provenance, Unset):
            provenance = self.provenance.to_dict()

        global_ref = self.global_ref

        prev_seq: int | None | Unset
        if isinstance(self.prev_seq, Unset):
            prev_seq = UNSET
        else:
            prev_seq = self.prev_seq

        history_tier: str | Unset = UNSET
        if not isinstance(self.history_tier, Unset):
            history_tier = self.history_tier.value

        batch_id: int | None | Unset
        if isinstance(self.batch_id, Unset):
            batch_id = UNSET
        else:
            batch_id = self.batch_id

        downsampled = self.downsampled

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "source": source,
                "externalId": external_id,
                "kind": kind,
                "version": version,
                "seq": seq,
                "observedAt": observed_at,
                "payload": payload,
                "ingestedAt": ingested_at,
                "persistReason": persist_reason,
                "receivedAt": received_at,
            }
        )
        if valid_until is not UNSET:
            field_dict["validUntil"] = valid_until
        if tombstone is not UNSET:
            field_dict["tombstone"] = tombstone
        if geometry is not UNSET:
            field_dict["geometry"] = geometry
        if attribution is not UNSET:
            field_dict["attribution"] = attribution
        if title is not UNSET:
            field_dict["title"] = title
        if persisted_observed_at is not UNSET:
            field_dict["persistedObservedAt"] = persisted_observed_at
        if event_time is not UNSET:
            field_dict["eventTime"] = event_time
        if valid_from is not UNSET:
            field_dict["validFrom"] = valid_from
        if quality is not UNSET:
            field_dict["quality"] = quality
        if live is not UNSET:
            field_dict["live"] = live
        if qa_flags is not UNSET:
            field_dict["qaFlags"] = qa_flags
        if distance_km is not UNSET:
            field_dict["distanceKm"] = distance_km
        if observer is not UNSET:
            field_dict["observer"] = observer
        if region is not UNSET:
            field_dict["region"] = region
        if entity_id is not UNSET:
            field_dict["entityId"] = entity_id
        if provenance is not UNSET:
            field_dict["provenance"] = provenance
        if global_ref is not UNSET:
            field_dict["globalRef"] = global_ref
        if prev_seq is not UNSET:
            field_dict["prevSeq"] = prev_seq
        if history_tier is not UNSET:
            field_dict["historyTier"] = history_tier
        if batch_id is not UNSET:
            field_dict["batchId"] = batch_id
        if downsampled is not UNSET:
            field_dict["downsampled"] = downsampled

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry
        from ..models.observer import Observer
        from ..models.provenance import Provenance
        from ..models.quality import Quality
        from ..models.record_payload import RecordPayload
        from ..models.region_tags import RegionTags

        d = dict(src_dict)
        id = d.pop("id")

        source = d.pop("source")

        external_id = d.pop("externalId")

        kind = d.pop("kind")

        version = d.pop("version")

        seq = d.pop("seq")

        observed_at = datetime.datetime.fromisoformat(d.pop("observedAt"))

        payload = RecordPayload.from_dict(d.pop("payload"))

        ingested_at = datetime.datetime.fromisoformat(d.pop("ingestedAt"))

        persist_reason = PersistReason(d.pop("persistReason"))

        received_at = datetime.datetime.fromisoformat(d.pop("receivedAt"))

        def _parse_valid_until(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                valid_until_type_0 = datetime.datetime.fromisoformat(data)

                return valid_until_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        valid_until = _parse_valid_until(d.pop("validUntil", UNSET))

        tombstone = d.pop("tombstone", UNSET)

        def _parse_geometry(data: object) -> Geometry | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                geometry_type_0 = Geometry.from_dict(data)

                return geometry_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Geometry | None | Unset, data)

        geometry = _parse_geometry(d.pop("geometry", UNSET))

        def _parse_attribution(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        attribution = _parse_attribution(d.pop("attribution", UNSET))

        def _parse_title(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        title = _parse_title(d.pop("title", UNSET))

        _persisted_observed_at = d.pop("persistedObservedAt", UNSET)
        persisted_observed_at: datetime.datetime | Unset
        if isinstance(_persisted_observed_at, Unset):
            persisted_observed_at = UNSET
        else:
            persisted_observed_at = datetime.datetime.fromisoformat(
                _persisted_observed_at
            )

        def _parse_event_time(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                event_time_type_0 = datetime.datetime.fromisoformat(data)

                return event_time_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        event_time = _parse_event_time(d.pop("eventTime", UNSET))

        def _parse_valid_from(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                valid_from_type_0 = datetime.datetime.fromisoformat(data)

                return valid_from_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        valid_from = _parse_valid_from(d.pop("validFrom", UNSET))

        def _parse_quality(data: object) -> None | Quality | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                quality_type_0 = Quality.from_dict(data)

                return quality_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Quality | Unset, data)

        quality = _parse_quality(d.pop("quality", UNSET))

        live = d.pop("live", UNSET)

        qa_flags = d.pop("qaFlags", UNSET)

        distance_km = d.pop("distanceKm", UNSET)

        _observer = d.pop("observer", UNSET)
        observer: list[Observer] | Unset = UNSET
        if _observer is not UNSET:
            observer = []
            for observer_item_data in _observer:
                observer_item = Observer.from_dict(observer_item_data)

                observer.append(observer_item)

        def _parse_region(data: object) -> None | RegionTags | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                region_type_0 = RegionTags.from_dict(data)

                return region_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | RegionTags | Unset, data)

        region = _parse_region(d.pop("region", UNSET))

        def _parse_entity_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        entity_id = _parse_entity_id(d.pop("entityId", UNSET))

        _provenance = d.pop("provenance", UNSET)
        provenance: Provenance | Unset
        if isinstance(_provenance, Unset):
            provenance = UNSET
        else:
            provenance = Provenance.from_dict(_provenance)

        global_ref = d.pop("globalRef", UNSET)

        def _parse_prev_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        prev_seq = _parse_prev_seq(d.pop("prevSeq", UNSET))

        _history_tier = d.pop("historyTier", UNSET)
        history_tier: HistoryTier | Unset
        if isinstance(_history_tier, Unset):
            history_tier = UNSET
        else:
            history_tier = HistoryTier(_history_tier)

        def _parse_batch_id(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        batch_id = _parse_batch_id(d.pop("batchId", UNSET))

        downsampled = d.pop("downsampled", UNSET)

        record_version = cls(
            id=id,
            source=source,
            external_id=external_id,
            kind=kind,
            version=version,
            seq=seq,
            observed_at=observed_at,
            payload=payload,
            ingested_at=ingested_at,
            persist_reason=persist_reason,
            received_at=received_at,
            valid_until=valid_until,
            tombstone=tombstone,
            geometry=geometry,
            attribution=attribution,
            title=title,
            persisted_observed_at=persisted_observed_at,
            event_time=event_time,
            valid_from=valid_from,
            quality=quality,
            live=live,
            qa_flags=qa_flags,
            distance_km=distance_km,
            observer=observer,
            region=region,
            entity_id=entity_id,
            provenance=provenance,
            global_ref=global_ref,
            prev_seq=prev_seq,
            history_tier=history_tier,
            batch_id=batch_id,
            downsampled=downsampled,
        )

        record_version.additional_properties = d
        return record_version

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
