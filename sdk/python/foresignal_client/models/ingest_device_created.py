from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.ingest_device_setup import IngestDeviceSetup


T = TypeVar("T", bound="IngestDeviceCreated")


@_attrs_define
class IngestDeviceCreated:
    """A device key returned once with app setup data (039)

    Attributes:
        id (UUID):
        source_id (str):
        external_id (str):
        name (str):
        key_prefix (str):  Example: eid_AbC1.
        created_at (datetime.datetime):
        key (str): `eid_…`; shown once
        setup (IngestDeviceSetup): Ready-to-use tracker app settings, based on publicBaseUrl (039)
        last_seen_at (datetime.datetime | None | Unset):
        revoked_at (datetime.datetime | None | Unset):
    """

    id: UUID
    source_id: str
    external_id: str
    name: str
    key_prefix: str
    created_at: datetime.datetime
    key: str
    setup: IngestDeviceSetup
    last_seen_at: datetime.datetime | None | Unset = UNSET
    revoked_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = str(self.id)

        source_id = self.source_id

        external_id = self.external_id

        name = self.name

        key_prefix = self.key_prefix

        created_at = self.created_at.isoformat()

        key = self.key

        setup = self.setup.to_dict()

        last_seen_at: None | str | Unset
        if isinstance(self.last_seen_at, Unset):
            last_seen_at = UNSET
        elif isinstance(self.last_seen_at, datetime.datetime):
            last_seen_at = self.last_seen_at.isoformat()
        else:
            last_seen_at = self.last_seen_at

        revoked_at: None | str | Unset
        if isinstance(self.revoked_at, Unset):
            revoked_at = UNSET
        elif isinstance(self.revoked_at, datetime.datetime):
            revoked_at = self.revoked_at.isoformat()
        else:
            revoked_at = self.revoked_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "sourceId": source_id,
                "externalId": external_id,
                "name": name,
                "keyPrefix": key_prefix,
                "createdAt": created_at,
                "key": key,
                "setup": setup,
            }
        )
        if last_seen_at is not UNSET:
            field_dict["lastSeenAt"] = last_seen_at
        if revoked_at is not UNSET:
            field_dict["revokedAt"] = revoked_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ingest_device_setup import IngestDeviceSetup

        d = dict(src_dict)
        id = UUID(d.pop("id"))

        source_id = d.pop("sourceId")

        external_id = d.pop("externalId")

        name = d.pop("name")

        key_prefix = d.pop("keyPrefix")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        key = d.pop("key")

        setup = IngestDeviceSetup.from_dict(d.pop("setup"))

        def _parse_last_seen_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_seen_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_seen_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_seen_at = _parse_last_seen_at(d.pop("lastSeenAt", UNSET))

        def _parse_revoked_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                revoked_at_type_0 = datetime.datetime.fromisoformat(data)

                return revoked_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        revoked_at = _parse_revoked_at(d.pop("revokedAt", UNSET))

        ingest_device_created = cls(
            id=id,
            source_id=source_id,
            external_id=external_id,
            name=name,
            key_prefix=key_prefix,
            created_at=created_at,
            key=key,
            setup=setup,
            last_seen_at=last_seen_at,
            revoked_at=revoked_at,
        )

        ingest_device_created.additional_properties = d
        return ingest_device_created

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
