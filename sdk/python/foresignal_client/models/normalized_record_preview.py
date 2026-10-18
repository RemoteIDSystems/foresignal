from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.normalized_record_preview_geometry_type_0 import (
        NormalizedRecordPreviewGeometryType0,
    )
    from ..models.normalized_record_preview_payload import (
        NormalizedRecordPreviewPayload,
    )
    from ..models.observer import Observer
    from ..models.quality import Quality


T = TypeVar("T", bound="NormalizedRecordPreview")


@_attrs_define
class NormalizedRecordPreview:
    """
    Attributes:
        external_id (str):
        kind (str):
        observed_at (datetime.datetime):
        payload (NormalizedRecordPreviewPayload):
        geometry (None | NormalizedRecordPreviewGeometryType0 | Unset): GeoJSON geometry (RFC 7946), WGS84
        valid_until (datetime.datetime | None | Unset):
        tombstone (bool | Unset):
        event_time (datetime.datetime | None | Unset):
        valid_from (datetime.datetime | None | Unset):
        quality (None | Quality | Unset):
        observers (list[Observer] | Unset):
    """

    external_id: str
    kind: str
    observed_at: datetime.datetime
    payload: NormalizedRecordPreviewPayload
    geometry: None | NormalizedRecordPreviewGeometryType0 | Unset = UNSET
    valid_until: datetime.datetime | None | Unset = UNSET
    tombstone: bool | Unset = UNSET
    event_time: datetime.datetime | None | Unset = UNSET
    valid_from: datetime.datetime | None | Unset = UNSET
    quality: None | Quality | Unset = UNSET
    observers: list[Observer] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.normalized_record_preview_geometry_type_0 import (
            NormalizedRecordPreviewGeometryType0,
        )
        from ..models.quality import Quality

        external_id = self.external_id

        kind = self.kind

        observed_at = self.observed_at.isoformat()

        payload = self.payload.to_dict()

        geometry: dict[str, Any] | None | Unset
        if isinstance(self.geometry, Unset):
            geometry = UNSET
        elif isinstance(self.geometry, NormalizedRecordPreviewGeometryType0):
            geometry = self.geometry.to_dict()
        else:
            geometry = self.geometry

        valid_until: None | str | Unset
        if isinstance(self.valid_until, Unset):
            valid_until = UNSET
        elif isinstance(self.valid_until, datetime.datetime):
            valid_until = self.valid_until.isoformat()
        else:
            valid_until = self.valid_until

        tombstone = self.tombstone

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

        observers: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.observers, Unset):
            observers = []
            for observers_item_data in self.observers:
                observers_item = observers_item_data.to_dict()
                observers.append(observers_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "externalId": external_id,
                "kind": kind,
                "observedAt": observed_at,
                "payload": payload,
            }
        )
        if geometry is not UNSET:
            field_dict["geometry"] = geometry
        if valid_until is not UNSET:
            field_dict["validUntil"] = valid_until
        if tombstone is not UNSET:
            field_dict["tombstone"] = tombstone
        if event_time is not UNSET:
            field_dict["eventTime"] = event_time
        if valid_from is not UNSET:
            field_dict["validFrom"] = valid_from
        if quality is not UNSET:
            field_dict["quality"] = quality
        if observers is not UNSET:
            field_dict["observers"] = observers

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.normalized_record_preview_geometry_type_0 import (
            NormalizedRecordPreviewGeometryType0,
        )
        from ..models.normalized_record_preview_payload import (
            NormalizedRecordPreviewPayload,
        )
        from ..models.observer import Observer
        from ..models.quality import Quality

        d = dict(src_dict)
        external_id = d.pop("externalId")

        kind = d.pop("kind")

        observed_at = datetime.datetime.fromisoformat(d.pop("observedAt"))

        payload = NormalizedRecordPreviewPayload.from_dict(d.pop("payload"))

        def _parse_geometry(
            data: object,
        ) -> None | NormalizedRecordPreviewGeometryType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                geometry_type_0 = NormalizedRecordPreviewGeometryType0.from_dict(data)

                return geometry_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | NormalizedRecordPreviewGeometryType0 | Unset, data)

        geometry = _parse_geometry(d.pop("geometry", UNSET))

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

        _observers = d.pop("observers", UNSET)
        observers: list[Observer] | Unset = UNSET
        if _observers is not UNSET:
            observers = []
            for observers_item_data in _observers:
                observers_item = Observer.from_dict(observers_item_data)

                observers.append(observers_item)

        normalized_record_preview = cls(
            external_id=external_id,
            kind=kind,
            observed_at=observed_at,
            payload=payload,
            geometry=geometry,
            valid_until=valid_until,
            tombstone=tombstone,
            event_time=event_time,
            valid_from=valid_from,
            quality=quality,
            observers=observers,
        )

        normalized_record_preview.additional_properties = d
        return normalized_record_preview

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
