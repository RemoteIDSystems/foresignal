from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.alert_record_snapshot_payload_type_0 import (
        AlertRecordSnapshotPayloadType0,
    )
    from ..models.geometry import Geometry


T = TypeVar("T", bound="AlertRecordSnapshot")


@_attrs_define
class AlertRecordSnapshot:
    """Copy of the matching version stored on the alert (≤ 16 KB; payload dropped when larger)

    Attributes:
        kind (str):
        version (int):
        observed_at (datetime.datetime):
        geometry (Geometry | None | Unset):
        payload (AlertRecordSnapshotPayloadType0 | None | Unset):
        truncated (bool | Unset): True when the payload was dropped for size Default: False.
    """

    kind: str
    version: int
    observed_at: datetime.datetime
    geometry: Geometry | None | Unset = UNSET
    payload: AlertRecordSnapshotPayloadType0 | None | Unset = UNSET
    truncated: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.alert_record_snapshot_payload_type_0 import (
            AlertRecordSnapshotPayloadType0,
        )
        from ..models.geometry import Geometry

        kind = self.kind

        version = self.version

        observed_at = self.observed_at.isoformat()

        geometry: dict[str, Any] | None | Unset
        if isinstance(self.geometry, Unset):
            geometry = UNSET
        elif isinstance(self.geometry, Geometry):
            geometry = self.geometry.to_dict()
        else:
            geometry = self.geometry

        payload: dict[str, Any] | None | Unset
        if isinstance(self.payload, Unset):
            payload = UNSET
        elif isinstance(self.payload, AlertRecordSnapshotPayloadType0):
            payload = self.payload.to_dict()
        else:
            payload = self.payload

        truncated = self.truncated

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "kind": kind,
                "version": version,
                "observedAt": observed_at,
            }
        )
        if geometry is not UNSET:
            field_dict["geometry"] = geometry
        if payload is not UNSET:
            field_dict["payload"] = payload
        if truncated is not UNSET:
            field_dict["truncated"] = truncated

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.alert_record_snapshot_payload_type_0 import (
            AlertRecordSnapshotPayloadType0,
        )
        from ..models.geometry import Geometry

        d = dict(src_dict)
        kind = d.pop("kind")

        version = d.pop("version")

        observed_at = datetime.datetime.fromisoformat(d.pop("observedAt"))

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

        def _parse_payload(
            data: object,
        ) -> AlertRecordSnapshotPayloadType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                payload_type_0 = AlertRecordSnapshotPayloadType0.from_dict(data)

                return payload_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(AlertRecordSnapshotPayloadType0 | None | Unset, data)

        payload = _parse_payload(d.pop("payload", UNSET))

        truncated = d.pop("truncated", UNSET)

        alert_record_snapshot = cls(
            kind=kind,
            version=version,
            observed_at=observed_at,
            geometry=geometry,
            payload=payload,
            truncated=truncated,
        )

        alert_record_snapshot.additional_properties = d
        return alert_record_snapshot

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
