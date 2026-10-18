from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.share_link_status import ShareLinkStatus
from ..models.share_link_target_type import ShareLinkTargetType
from ..types import UNSET, Unset

T = TypeVar("T", bound="ShareLinkCreated")


@_attrs_define
class ShareLinkCreated:
    """A newly created share link with its plaintext key (shown once) (112)

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        key_prefix (str): First characters of the fss_ key
        target_type (ShareLinkTargetType):
        target_ref (str):
        expires_at (datetime.datetime):
        uses (int):
        status (ShareLinkStatus):
        created_at (datetime.datetime):
        key (str):
        url (str): {publicBaseUrl}/v1/k/{key}
        field_mask (list[str] | None | Unset):
        precision_m (int | None | Unset):
        masked (bool | Unset): Privacy mask applied (personal or private source)
        max_uses (int | None | Unset):
        created_by (str | Unset):
        revoked_at (datetime.datetime | None | Unset):
        last_used_at (datetime.datetime | None | Unset):
    """

    id: str
    key_prefix: str
    target_type: ShareLinkTargetType
    target_ref: str
    expires_at: datetime.datetime
    uses: int
    status: ShareLinkStatus
    created_at: datetime.datetime
    key: str
    url: str
    field_mask: list[str] | None | Unset = UNSET
    precision_m: int | None | Unset = UNSET
    masked: bool | Unset = UNSET
    max_uses: int | None | Unset = UNSET
    created_by: str | Unset = UNSET
    revoked_at: datetime.datetime | None | Unset = UNSET
    last_used_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        key_prefix = self.key_prefix

        target_type = self.target_type.value

        target_ref = self.target_ref

        expires_at = self.expires_at.isoformat()

        uses = self.uses

        status = self.status.value

        created_at = self.created_at.isoformat()

        key = self.key

        url = self.url

        field_mask: list[str] | None | Unset
        if isinstance(self.field_mask, Unset):
            field_mask = UNSET
        elif isinstance(self.field_mask, list):
            field_mask = self.field_mask

        else:
            field_mask = self.field_mask

        precision_m: int | None | Unset
        if isinstance(self.precision_m, Unset):
            precision_m = UNSET
        else:
            precision_m = self.precision_m

        masked = self.masked

        max_uses: int | None | Unset
        if isinstance(self.max_uses, Unset):
            max_uses = UNSET
        else:
            max_uses = self.max_uses

        created_by = self.created_by

        revoked_at: None | str | Unset
        if isinstance(self.revoked_at, Unset):
            revoked_at = UNSET
        elif isinstance(self.revoked_at, datetime.datetime):
            revoked_at = self.revoked_at.isoformat()
        else:
            revoked_at = self.revoked_at

        last_used_at: None | str | Unset
        if isinstance(self.last_used_at, Unset):
            last_used_at = UNSET
        elif isinstance(self.last_used_at, datetime.datetime):
            last_used_at = self.last_used_at.isoformat()
        else:
            last_used_at = self.last_used_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "keyPrefix": key_prefix,
                "targetType": target_type,
                "targetRef": target_ref,
                "expiresAt": expires_at,
                "uses": uses,
                "status": status,
                "createdAt": created_at,
                "key": key,
                "url": url,
            }
        )
        if field_mask is not UNSET:
            field_dict["fieldMask"] = field_mask
        if precision_m is not UNSET:
            field_dict["precisionM"] = precision_m
        if masked is not UNSET:
            field_dict["masked"] = masked
        if max_uses is not UNSET:
            field_dict["maxUses"] = max_uses
        if created_by is not UNSET:
            field_dict["createdBy"] = created_by
        if revoked_at is not UNSET:
            field_dict["revokedAt"] = revoked_at
        if last_used_at is not UNSET:
            field_dict["lastUsedAt"] = last_used_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        key_prefix = d.pop("keyPrefix")

        target_type = ShareLinkTargetType(d.pop("targetType"))

        target_ref = d.pop("targetRef")

        expires_at = datetime.datetime.fromisoformat(d.pop("expiresAt"))

        uses = d.pop("uses")

        status = ShareLinkStatus(d.pop("status"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        key = d.pop("key")

        url = d.pop("url")

        def _parse_field_mask(data: object) -> list[str] | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, list):
                    raise TypeError()
                field_mask_type_0 = cast(list[str], data)

                return field_mask_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(list[str] | None | Unset, data)

        field_mask = _parse_field_mask(d.pop("fieldMask", UNSET))

        def _parse_precision_m(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        precision_m = _parse_precision_m(d.pop("precisionM", UNSET))

        masked = d.pop("masked", UNSET)

        def _parse_max_uses(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        max_uses = _parse_max_uses(d.pop("maxUses", UNSET))

        created_by = d.pop("createdBy", UNSET)

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

        share_link_created = cls(
            id=id,
            key_prefix=key_prefix,
            target_type=target_type,
            target_ref=target_ref,
            expires_at=expires_at,
            uses=uses,
            status=status,
            created_at=created_at,
            key=key,
            url=url,
            field_mask=field_mask,
            precision_m=precision_m,
            masked=masked,
            max_uses=max_uses,
            created_by=created_by,
            revoked_at=revoked_at,
            last_used_at=last_used_at,
        )

        share_link_created.additional_properties = d
        return share_link_created

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
