from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.tile_key_prefixes_item import TileKeyPrefixesItem
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.read_restriction import ReadRestriction


T = TypeVar("T", bound="TileKeyCreated")


@_attrs_define
class TileKeyCreated:
    """Created tile key with its plaintext value (returned once)

    Attributes:
        id (UUID):
        name (str):
        key_prefix (str): First characters of the key (identification only) Example: fsk_AbC1.
        prefixes (list[TileKeyPrefixesItem]): Canonical path prefixes the key may read
        created_at (datetime.datetime):
        expires_at (datetime.datetime):
        key (str): Plaintext `fsk_…` key, shown once
        url_base (str): `{publicBaseUrl}/v1/k/{key}`; append the canonical path after `/v1/`
        restrictions (None | ReadRestriction | Unset):
        last_used_at (datetime.datetime | None | Unset):
        revoked_at (datetime.datetime | None | Unset):
    """

    id: UUID
    name: str
    key_prefix: str
    prefixes: list[TileKeyPrefixesItem]
    created_at: datetime.datetime
    expires_at: datetime.datetime
    key: str
    url_base: str
    restrictions: None | ReadRestriction | Unset = UNSET
    last_used_at: datetime.datetime | None | Unset = UNSET
    revoked_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.read_restriction import ReadRestriction

        id = str(self.id)

        name = self.name

        key_prefix = self.key_prefix

        prefixes = []
        for prefixes_item_data in self.prefixes:
            prefixes_item = prefixes_item_data.value
            prefixes.append(prefixes_item)

        created_at = self.created_at.isoformat()

        expires_at = self.expires_at.isoformat()

        key = self.key

        url_base = self.url_base

        restrictions: dict[str, Any] | None | Unset
        if isinstance(self.restrictions, Unset):
            restrictions = UNSET
        elif isinstance(self.restrictions, ReadRestriction):
            restrictions = self.restrictions.to_dict()
        else:
            restrictions = self.restrictions

        last_used_at: None | str | Unset
        if isinstance(self.last_used_at, Unset):
            last_used_at = UNSET
        elif isinstance(self.last_used_at, datetime.datetime):
            last_used_at = self.last_used_at.isoformat()
        else:
            last_used_at = self.last_used_at

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
                "name": name,
                "keyPrefix": key_prefix,
                "prefixes": prefixes,
                "createdAt": created_at,
                "expiresAt": expires_at,
                "key": key,
                "urlBase": url_base,
            }
        )
        if restrictions is not UNSET:
            field_dict["restrictions"] = restrictions
        if last_used_at is not UNSET:
            field_dict["lastUsedAt"] = last_used_at
        if revoked_at is not UNSET:
            field_dict["revokedAt"] = revoked_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.read_restriction import ReadRestriction

        d = dict(src_dict)
        id = UUID(d.pop("id"))

        name = d.pop("name")

        key_prefix = d.pop("keyPrefix")

        prefixes = []
        _prefixes = d.pop("prefixes")
        for prefixes_item_data in _prefixes:
            prefixes_item = TileKeyPrefixesItem(prefixes_item_data)

            prefixes.append(prefixes_item)

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        expires_at = datetime.datetime.fromisoformat(d.pop("expiresAt"))

        key = d.pop("key")

        url_base = d.pop("urlBase")

        def _parse_restrictions(data: object) -> None | ReadRestriction | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                restrictions_type_0 = ReadRestriction.from_dict(data)

                return restrictions_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | ReadRestriction | Unset, data)

        restrictions = _parse_restrictions(d.pop("restrictions", UNSET))

        def _parse_last_used_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_used_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_used_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_used_at = _parse_last_used_at(d.pop("lastUsedAt", UNSET))

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

        tile_key_created = cls(
            id=id,
            name=name,
            key_prefix=key_prefix,
            prefixes=prefixes,
            created_at=created_at,
            expires_at=expires_at,
            key=key,
            url_base=url_base,
            restrictions=restrictions,
            last_used_at=last_used_at,
            revoked_at=revoked_at,
        )

        tile_key_created.additional_properties = d
        return tile_key_created

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
