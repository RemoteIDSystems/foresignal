from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.read_restriction import ReadRestriction


T = TypeVar("T", bound="FeedKey")


@_attrs_define
class FeedKey:
    """A feed key (`fsf_`, scope read); the key value is never listed (035)

    Attributes:
        id (UUID):
        name (str):
        key_prefix (str):  Example: fsf_AbC1.
        prefixes (list[str]):
        restrictions (ReadRestriction): The effective read restriction of a principal (035): restricted tokens, feed
            keys and the anonymous profile. Intersected with every request filter by `RestrictionClamp`; out-of-scope single
            reads return 404.
        created_at (datetime.datetime):
        expires_at (datetime.datetime):
        private_access (bool | Unset):
        last_used_at (datetime.datetime | None | Unset):
        revoked_at (datetime.datetime | None | Unset):
    """

    id: UUID
    name: str
    key_prefix: str
    prefixes: list[str]
    restrictions: ReadRestriction
    created_at: datetime.datetime
    expires_at: datetime.datetime
    private_access: bool | Unset = UNSET
    last_used_at: datetime.datetime | None | Unset = UNSET
    revoked_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = str(self.id)

        name = self.name

        key_prefix = self.key_prefix

        prefixes = self.prefixes

        restrictions = self.restrictions.to_dict()

        created_at = self.created_at.isoformat()

        expires_at = self.expires_at.isoformat()

        private_access = self.private_access

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
                "restrictions": restrictions,
                "createdAt": created_at,
                "expiresAt": expires_at,
            }
        )
        if private_access is not UNSET:
            field_dict["privateAccess"] = private_access
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

        prefixes = cast(list[str], d.pop("prefixes"))

        restrictions = ReadRestriction.from_dict(d.pop("restrictions"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        expires_at = datetime.datetime.fromisoformat(d.pop("expiresAt"))

        private_access = d.pop("privateAccess", UNSET)

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

        feed_key = cls(
            id=id,
            name=name,
            key_prefix=key_prefix,
            prefixes=prefixes,
            restrictions=restrictions,
            created_at=created_at,
            expires_at=expires_at,
            private_access=private_access,
            last_used_at=last_used_at,
            revoked_at=revoked_at,
        )

        feed_key.additional_properties = d
        return feed_key

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
