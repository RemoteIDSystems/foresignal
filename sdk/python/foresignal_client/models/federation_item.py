from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.federation_item_history_tier import FederationItemHistoryTier
from ..models.federation_item_persist_reason import FederationItemPersistReason
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.federation_item_payload import FederationItemPayload
    from ..models.geometry import Geometry
    from ..models.provenance import Provenance
    from ..models.quality import Quality


T = TypeVar("T", bound="FederationItem")


@_attrs_define
class FederationItem:
    """A full version item served to peers, with licence id and provenance chain (113)

    Attributes:
        seq (int):
        ref (str): urn:foresignal:{instanceId}:{sourceId}:{externalId} (externalId percent-encoded)
        source_id (str):
        external_id (str):
        kind (str):
        version (int):
        observed_at (datetime.datetime):
        tombstone (bool):
        payload (FederationItemPayload):
        licence (str): LicenceTerms id in force for this version
        provenance (Provenance):
        persist_reason (FederationItemPersistReason | Unset):
        history_tier (FederationItemHistoryTier | Unset): The version's history tier; a consumer stores TRACK items in
            its peer-{id}-trk source (113)
        event_time (datetime.datetime | None | Unset):
        valid_until (datetime.datetime | None | Unset):
        geometry (Geometry | None | Unset):
        quality (None | Quality | Unset):
    """

    seq: int
    ref: str
    source_id: str
    external_id: str
    kind: str
    version: int
    observed_at: datetime.datetime
    tombstone: bool
    payload: FederationItemPayload
    licence: str
    provenance: Provenance
    persist_reason: FederationItemPersistReason | Unset = UNSET
    history_tier: FederationItemHistoryTier | Unset = UNSET
    event_time: datetime.datetime | None | Unset = UNSET
    valid_until: datetime.datetime | None | Unset = UNSET
    geometry: Geometry | None | Unset = UNSET
    quality: None | Quality | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.geometry import Geometry
        from ..models.quality import Quality

        seq = self.seq

        ref = self.ref

        source_id = self.source_id

        external_id = self.external_id

        kind = self.kind

        version = self.version

        observed_at = self.observed_at.isoformat()

        tombstone = self.tombstone

        payload = self.payload.to_dict()

        licence = self.licence

        provenance = self.provenance.to_dict()

        persist_reason: str | Unset = UNSET
        if not isinstance(self.persist_reason, Unset):
            persist_reason = self.persist_reason.value

        history_tier: str | Unset = UNSET
        if not isinstance(self.history_tier, Unset):
            history_tier = self.history_tier.value

        event_time: None | str | Unset
        if isinstance(self.event_time, Unset):
            event_time = UNSET
        elif isinstance(self.event_time, datetime.datetime):
            event_time = self.event_time.isoformat()
        else:
            event_time = self.event_time

        valid_until: None | str | Unset
        if isinstance(self.valid_until, Unset):
            valid_until = UNSET
        elif isinstance(self.valid_until, datetime.datetime):
            valid_until = self.valid_until.isoformat()
        else:
            valid_until = self.valid_until

        geometry: dict[str, Any] | None | Unset
        if isinstance(self.geometry, Unset):
            geometry = UNSET
        elif isinstance(self.geometry, Geometry):
            geometry = self.geometry.to_dict()
        else:
            geometry = self.geometry

        quality: dict[str, Any] | None | Unset
        if isinstance(self.quality, Unset):
            quality = UNSET
        elif isinstance(self.quality, Quality):
            quality = self.quality.to_dict()
        else:
            quality = self.quality

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "seq": seq,
                "ref": ref,
                "sourceId": source_id,
                "externalId": external_id,
                "kind": kind,
                "version": version,
                "observedAt": observed_at,
                "tombstone": tombstone,
                "payload": payload,
                "licence": licence,
                "provenance": provenance,
            }
        )
        if persist_reason is not UNSET:
            field_dict["persistReason"] = persist_reason
        if history_tier is not UNSET:
            field_dict["historyTier"] = history_tier
        if event_time is not UNSET:
            field_dict["eventTime"] = event_time
        if valid_until is not UNSET:
            field_dict["validUntil"] = valid_until
        if geometry is not UNSET:
            field_dict["geometry"] = geometry
        if quality is not UNSET:
            field_dict["quality"] = quality

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.federation_item_payload import FederationItemPayload
        from ..models.geometry import Geometry
        from ..models.provenance import Provenance
        from ..models.quality import Quality

        d = dict(src_dict)
        seq = d.pop("seq")

        ref = d.pop("ref")

        source_id = d.pop("sourceId")

        external_id = d.pop("externalId")

        kind = d.pop("kind")

        version = d.pop("version")

        observed_at = datetime.datetime.fromisoformat(d.pop("observedAt"))

        tombstone = d.pop("tombstone")

        payload = FederationItemPayload.from_dict(d.pop("payload"))

        licence = d.pop("licence")

        provenance = Provenance.from_dict(d.pop("provenance"))

        _persist_reason = d.pop("persistReason", UNSET)
        persist_reason: FederationItemPersistReason | Unset
        if isinstance(_persist_reason, Unset):
            persist_reason = UNSET
        else:
            persist_reason = FederationItemPersistReason(_persist_reason)

        _history_tier = d.pop("historyTier", UNSET)
        history_tier: FederationItemHistoryTier | Unset
        if isinstance(_history_tier, Unset):
            history_tier = UNSET
        else:
            history_tier = FederationItemHistoryTier(_history_tier)

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

        federation_item = cls(
            seq=seq,
            ref=ref,
            source_id=source_id,
            external_id=external_id,
            kind=kind,
            version=version,
            observed_at=observed_at,
            tombstone=tombstone,
            payload=payload,
            licence=licence,
            provenance=provenance,
            persist_reason=persist_reason,
            history_tier=history_tier,
            event_time=event_time,
            valid_until=valid_until,
            geometry=geometry,
            quality=quality,
        )

        federation_item.additional_properties = d
        return federation_item

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
