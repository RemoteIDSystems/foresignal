from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.persist_reason import PersistReason
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.feature_properties_payload import FeaturePropertiesPayload


T = TypeVar("T", bound="FeatureProperties")


@_attrs_define
class FeatureProperties:
    """
    Attributes:
        source (str | Unset):
        external_id (str | Unset):
        kind (str | Unset):
        title (None | str | Unset):
        version (int | Unset):
        seq (int | Unset):
        observed_at (datetime.datetime | Unset):
        valid_until (datetime.datetime | None | Unset):
        live (bool | Unset): record_live.dirty
        distance_km (float | Unset):
        payload (FeaturePropertiesPayload | Unset):
        near (None | str | Unset): Region near text with include=region (040)
        ingested_at (datetime.datetime | Unset): Version-based features only (history and asOf): the batch time of the
            version
        persist_reason (PersistReason | Unset): Why a version was persisted (Core-Data-Model §5.2; stored codes 1–9)
    """

    source: str | Unset = UNSET
    external_id: str | Unset = UNSET
    kind: str | Unset = UNSET
    title: None | str | Unset = UNSET
    version: int | Unset = UNSET
    seq: int | Unset = UNSET
    observed_at: datetime.datetime | Unset = UNSET
    valid_until: datetime.datetime | None | Unset = UNSET
    live: bool | Unset = UNSET
    distance_km: float | Unset = UNSET
    payload: FeaturePropertiesPayload | Unset = UNSET
    near: None | str | Unset = UNSET
    ingested_at: datetime.datetime | Unset = UNSET
    persist_reason: PersistReason | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source = self.source

        external_id = self.external_id

        kind = self.kind

        title: None | str | Unset
        if isinstance(self.title, Unset):
            title = UNSET
        else:
            title = self.title

        version = self.version

        seq = self.seq

        observed_at: str | Unset = UNSET
        if not isinstance(self.observed_at, Unset):
            observed_at = self.observed_at.isoformat()

        valid_until: None | str | Unset
        if isinstance(self.valid_until, Unset):
            valid_until = UNSET
        elif isinstance(self.valid_until, datetime.datetime):
            valid_until = self.valid_until.isoformat()
        else:
            valid_until = self.valid_until

        live = self.live

        distance_km = self.distance_km

        payload: dict[str, Any] | Unset = UNSET
        if not isinstance(self.payload, Unset):
            payload = self.payload.to_dict()

        near: None | str | Unset
        if isinstance(self.near, Unset):
            near = UNSET
        else:
            near = self.near

        ingested_at: str | Unset = UNSET
        if not isinstance(self.ingested_at, Unset):
            ingested_at = self.ingested_at.isoformat()

        persist_reason: str | Unset = UNSET
        if not isinstance(self.persist_reason, Unset):
            persist_reason = self.persist_reason.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if source is not UNSET:
            field_dict["source"] = source
        if external_id is not UNSET:
            field_dict["externalId"] = external_id
        if kind is not UNSET:
            field_dict["kind"] = kind
        if title is not UNSET:
            field_dict["title"] = title
        if version is not UNSET:
            field_dict["version"] = version
        if seq is not UNSET:
            field_dict["seq"] = seq
        if observed_at is not UNSET:
            field_dict["observedAt"] = observed_at
        if valid_until is not UNSET:
            field_dict["validUntil"] = valid_until
        if live is not UNSET:
            field_dict["live"] = live
        if distance_km is not UNSET:
            field_dict["distanceKm"] = distance_km
        if payload is not UNSET:
            field_dict["payload"] = payload
        if near is not UNSET:
            field_dict["near"] = near
        if ingested_at is not UNSET:
            field_dict["ingestedAt"] = ingested_at
        if persist_reason is not UNSET:
            field_dict["persistReason"] = persist_reason

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.feature_properties_payload import FeaturePropertiesPayload

        d = dict(src_dict)
        source = d.pop("source", UNSET)

        external_id = d.pop("externalId", UNSET)

        kind = d.pop("kind", UNSET)

        def _parse_title(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        title = _parse_title(d.pop("title", UNSET))

        version = d.pop("version", UNSET)

        seq = d.pop("seq", UNSET)

        _observed_at = d.pop("observedAt", UNSET)
        observed_at: datetime.datetime | Unset
        if isinstance(_observed_at, Unset):
            observed_at = UNSET
        else:
            observed_at = datetime.datetime.fromisoformat(_observed_at)

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

        live = d.pop("live", UNSET)

        distance_km = d.pop("distanceKm", UNSET)

        _payload = d.pop("payload", UNSET)
        payload: FeaturePropertiesPayload | Unset
        if isinstance(_payload, Unset):
            payload = UNSET
        else:
            payload = FeaturePropertiesPayload.from_dict(_payload)

        def _parse_near(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        near = _parse_near(d.pop("near", UNSET))

        _ingested_at = d.pop("ingestedAt", UNSET)
        ingested_at: datetime.datetime | Unset
        if isinstance(_ingested_at, Unset):
            ingested_at = UNSET
        else:
            ingested_at = datetime.datetime.fromisoformat(_ingested_at)

        _persist_reason = d.pop("persistReason", UNSET)
        persist_reason: PersistReason | Unset
        if isinstance(_persist_reason, Unset):
            persist_reason = UNSET
        else:
            persist_reason = PersistReason(_persist_reason)

        feature_properties = cls(
            source=source,
            external_id=external_id,
            kind=kind,
            title=title,
            version=version,
            seq=seq,
            observed_at=observed_at,
            valid_until=valid_until,
            live=live,
            distance_km=distance_km,
            payload=payload,
            near=near,
            ingested_at=ingested_at,
            persist_reason=persist_reason,
        )

        feature_properties.additional_properties = d
        return feature_properties

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
