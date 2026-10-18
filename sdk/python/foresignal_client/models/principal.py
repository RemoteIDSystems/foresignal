from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.principal_kind import PrincipalKind
from ..types import UNSET, Unset

T = TypeVar("T", bound="Principal")


@_attrs_define
class Principal:
    """A person, agent or service that holds scoped tokens; no passwords are stored

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        handle (str):  Example: jsmith.
        display_name (str):
        kind (PrincipalKind):
        disabled (bool):
        created_at (datetime.datetime):
        email (None | str | Unset): Used only for this principal's email notifications; returned only to the principal
            itself and admins
        avatar_media_id (None | str | Unset): 043 media id
        timezone (None | str | Unset):  Example: Europe/London.
        organization (None | str | Unset): Free text; defines TLP AMBER and AMBER+STRICT boundaries
        token_count (int | Unset): Active tokens (admin view)
        updated_at (datetime.datetime | Unset):
    """

    id: str
    handle: str
    display_name: str
    kind: PrincipalKind
    disabled: bool
    created_at: datetime.datetime
    email: None | str | Unset = UNSET
    avatar_media_id: None | str | Unset = UNSET
    timezone: None | str | Unset = UNSET
    organization: None | str | Unset = UNSET
    token_count: int | Unset = UNSET
    updated_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        handle = self.handle

        display_name = self.display_name

        kind = self.kind.value

        disabled = self.disabled

        created_at = self.created_at.isoformat()

        email: None | str | Unset
        if isinstance(self.email, Unset):
            email = UNSET
        else:
            email = self.email

        avatar_media_id: None | str | Unset
        if isinstance(self.avatar_media_id, Unset):
            avatar_media_id = UNSET
        else:
            avatar_media_id = self.avatar_media_id

        timezone: None | str | Unset
        if isinstance(self.timezone, Unset):
            timezone = UNSET
        else:
            timezone = self.timezone

        organization: None | str | Unset
        if isinstance(self.organization, Unset):
            organization = UNSET
        else:
            organization = self.organization

        token_count = self.token_count

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "handle": handle,
                "displayName": display_name,
                "kind": kind,
                "disabled": disabled,
                "createdAt": created_at,
            }
        )
        if email is not UNSET:
            field_dict["email"] = email
        if avatar_media_id is not UNSET:
            field_dict["avatarMediaId"] = avatar_media_id
        if timezone is not UNSET:
            field_dict["timezone"] = timezone
        if organization is not UNSET:
            field_dict["organization"] = organization
        if token_count is not UNSET:
            field_dict["tokenCount"] = token_count
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        handle = d.pop("handle")

        display_name = d.pop("displayName")

        kind = PrincipalKind(d.pop("kind"))

        disabled = d.pop("disabled")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        def _parse_email(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        email = _parse_email(d.pop("email", UNSET))

        def _parse_avatar_media_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        avatar_media_id = _parse_avatar_media_id(d.pop("avatarMediaId", UNSET))

        def _parse_timezone(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        timezone = _parse_timezone(d.pop("timezone", UNSET))

        def _parse_organization(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        organization = _parse_organization(d.pop("organization", UNSET))

        token_count = d.pop("tokenCount", UNSET)

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

        principal = cls(
            id=id,
            handle=handle,
            display_name=display_name,
            kind=kind,
            disabled=disabled,
            created_at=created_at,
            email=email,
            avatar_media_id=avatar_media_id,
            timezone=timezone,
            organization=organization,
            token_count=token_count,
            updated_at=updated_at,
        )

        principal.additional_properties = d
        return principal

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
