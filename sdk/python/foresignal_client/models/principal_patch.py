from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="PrincipalPatch")


@_attrs_define
class PrincipalPatch:
    """JSON merge patch for a principal (admin); the handle is immutable

    Attributes:
        display_name (str | Unset):
        email (None | str | Unset):
        avatar_media_id (None | str | Unset):
        timezone (None | str | Unset):
        organization (None | str | Unset):
        disabled (bool | Unset): Disabled principals' tokens stop working
    """

    display_name: str | Unset = UNSET
    email: None | str | Unset = UNSET
    avatar_media_id: None | str | Unset = UNSET
    timezone: None | str | Unset = UNSET
    organization: None | str | Unset = UNSET
    disabled: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        display_name = self.display_name

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

        disabled = self.disabled

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if display_name is not UNSET:
            field_dict["displayName"] = display_name
        if email is not UNSET:
            field_dict["email"] = email
        if avatar_media_id is not UNSET:
            field_dict["avatarMediaId"] = avatar_media_id
        if timezone is not UNSET:
            field_dict["timezone"] = timezone
        if organization is not UNSET:
            field_dict["organization"] = organization
        if disabled is not UNSET:
            field_dict["disabled"] = disabled

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        display_name = d.pop("displayName", UNSET)

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

        disabled = d.pop("disabled", UNSET)

        principal_patch = cls(
            display_name=display_name,
            email=email,
            avatar_media_id=avatar_media_id,
            timezone=timezone,
            organization=organization,
            disabled=disabled,
        )

        principal_patch.additional_properties = d
        return principal_patch

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
