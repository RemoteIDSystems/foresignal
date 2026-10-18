from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.principal_input_kind import PrincipalInputKind
from ..types import UNSET, Unset

T = TypeVar("T", bound="PrincipalInput")


@_attrs_define
class PrincipalInput:
    """Create a principal (admin)

    Attributes:
        handle (str):  Example: jsmith.
        display_name (str):
        kind (PrincipalInputKind):
        email (None | str | Unset):
        avatar_media_id (None | str | Unset):
        timezone (None | str | Unset):
        organization (None | str | Unset):
    """

    handle: str
    display_name: str
    kind: PrincipalInputKind
    email: None | str | Unset = UNSET
    avatar_media_id: None | str | Unset = UNSET
    timezone: None | str | Unset = UNSET
    organization: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        handle = self.handle

        display_name = self.display_name

        kind = self.kind.value

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

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "handle": handle,
                "displayName": display_name,
                "kind": kind,
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

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        handle = d.pop("handle")

        display_name = d.pop("displayName")

        kind = PrincipalInputKind(d.pop("kind"))

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

        principal_input = cls(
            handle=handle,
            display_name=display_name,
            kind=kind,
            email=email,
            avatar_media_id=avatar_media_id,
            timezone=timezone,
            organization=organization,
        )

        principal_input.additional_properties = d
        return principal_input

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
