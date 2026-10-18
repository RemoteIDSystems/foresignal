from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.chronicle_record_version_payload import ChronicleRecordVersionPayload
    from ..models.geometry import Geometry


T = TypeVar("T", bound="ChronicleRecordVersion")


@_attrs_define
class ChronicleRecordVersion:
    """A record version captured in chronicle storage

    Attributes:
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        source_id (str):
        external_id (str):
        kind (str):
        seq (int):
        observed_at (datetime.datetime):
        layer_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        event_time (datetime.datetime | None | Unset):
        ingested_at (datetime.datetime | Unset):
        geometry (Geometry | None | Unset):
        payload (ChronicleRecordVersionPayload | Unset):
        persist_reason (str | Unset):
        backfilled (bool | Unset):
    """

    ref: str
    source_id: str
    external_id: str
    kind: str
    seq: int
    observed_at: datetime.datetime
    layer_id: str
    event_time: datetime.datetime | None | Unset = UNSET
    ingested_at: datetime.datetime | Unset = UNSET
    geometry: Geometry | None | Unset = UNSET
    payload: ChronicleRecordVersionPayload | Unset = UNSET
    persist_reason: str | Unset = UNSET
    backfilled: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.geometry import Geometry

        ref = self.ref

        source_id = self.source_id

        external_id = self.external_id

        kind = self.kind

        seq = self.seq

        observed_at = self.observed_at.isoformat()

        layer_id = self.layer_id

        event_time: None | str | Unset
        if isinstance(self.event_time, Unset):
            event_time = UNSET
        elif isinstance(self.event_time, datetime.datetime):
            event_time = self.event_time.isoformat()
        else:
            event_time = self.event_time

        ingested_at: str | Unset = UNSET
        if not isinstance(self.ingested_at, Unset):
            ingested_at = self.ingested_at.isoformat()

        geometry: dict[str, Any] | None | Unset
        if isinstance(self.geometry, Unset):
            geometry = UNSET
        elif isinstance(self.geometry, Geometry):
            geometry = self.geometry.to_dict()
        else:
            geometry = self.geometry

        payload: dict[str, Any] | Unset = UNSET
        if not isinstance(self.payload, Unset):
            payload = self.payload.to_dict()

        persist_reason = self.persist_reason

        backfilled = self.backfilled

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ref": ref,
                "sourceId": source_id,
                "externalId": external_id,
                "kind": kind,
                "seq": seq,
                "observedAt": observed_at,
                "layerId": layer_id,
            }
        )
        if event_time is not UNSET:
            field_dict["eventTime"] = event_time
        if ingested_at is not UNSET:
            field_dict["ingestedAt"] = ingested_at
        if geometry is not UNSET:
            field_dict["geometry"] = geometry
        if payload is not UNSET:
            field_dict["payload"] = payload
        if persist_reason is not UNSET:
            field_dict["persistReason"] = persist_reason
        if backfilled is not UNSET:
            field_dict["backfilled"] = backfilled

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.chronicle_record_version_payload import (
            ChronicleRecordVersionPayload,
        )
        from ..models.geometry import Geometry

        d = dict(src_dict)
        ref = d.pop("ref")

        source_id = d.pop("sourceId")

        external_id = d.pop("externalId")

        kind = d.pop("kind")

        seq = d.pop("seq")

        observed_at = datetime.datetime.fromisoformat(d.pop("observedAt"))

        layer_id = d.pop("layerId")

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

        _ingested_at = d.pop("ingestedAt", UNSET)
        ingested_at: datetime.datetime | Unset
        if isinstance(_ingested_at, Unset):
            ingested_at = UNSET
        else:
            ingested_at = datetime.datetime.fromisoformat(_ingested_at)

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

        _payload = d.pop("payload", UNSET)
        payload: ChronicleRecordVersionPayload | Unset
        if isinstance(_payload, Unset):
            payload = UNSET
        else:
            payload = ChronicleRecordVersionPayload.from_dict(_payload)

        persist_reason = d.pop("persistReason", UNSET)

        backfilled = d.pop("backfilled", UNSET)

        chronicle_record_version = cls(
            ref=ref,
            source_id=source_id,
            external_id=external_id,
            kind=kind,
            seq=seq,
            observed_at=observed_at,
            layer_id=layer_id,
            event_time=event_time,
            ingested_at=ingested_at,
            geometry=geometry,
            payload=payload,
            persist_reason=persist_reason,
            backfilled=backfilled,
        )

        chronicle_record_version.additional_properties = d
        return chronicle_record_version

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
