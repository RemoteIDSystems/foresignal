from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.user_data_restriction import UserDataRestriction


T = TypeVar("T", bound="UserUpdate")


@_attrs_define
class UserUpdate:
    """Update a user's profile fields (roles are set via setUserRoles; status via suspend/unsuspend)

    Attributes:
        full_name (None | str | Unset):
        email (None | str | Unset):
        data_restriction (None | Unset | UserDataRestriction): Replaces the user's data restriction; null lifts it,
            absent leaves it (R2-013)
    """

    full_name: None | str | Unset = UNSET
    email: None | str | Unset = UNSET
    data_restriction: None | Unset | UserDataRestriction = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.user_data_restriction import UserDataRestriction

        full_name: None | str | Unset
        if isinstance(self.full_name, Unset):
            full_name = UNSET
        else:
            full_name = self.full_name

        email: None | str | Unset
        if isinstance(self.email, Unset):
            email = UNSET
        else:
            email = self.email

        data_restriction: dict[str, Any] | None | Unset
        if isinstance(self.data_restriction, Unset):
            data_restriction = UNSET
        elif isinstance(self.data_restriction, UserDataRestriction):
            data_restriction = self.data_restriction.to_dict()
        else:
            data_restriction = self.data_restriction

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if full_name is not UNSET:
            field_dict["fullName"] = full_name
        if email is not UNSET:
            field_dict["email"] = email
        if data_restriction is not UNSET:
            field_dict["dataRestriction"] = data_restriction

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.user_data_restriction import UserDataRestriction

        d = dict(src_dict)

        def _parse_full_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        full_name = _parse_full_name(d.pop("fullName", UNSET))

        def _parse_email(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        email = _parse_email(d.pop("email", UNSET))

        def _parse_data_restriction(data: object) -> None | Unset | UserDataRestriction:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                data_restriction_type_0 = UserDataRestriction.from_dict(data)

                return data_restriction_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Unset | UserDataRestriction, data)

        data_restriction = _parse_data_restriction(d.pop("dataRestriction", UNSET))

        user_update = cls(
            full_name=full_name,
            email=email,
            data_restriction=data_restriction,
        )

        user_update.additional_properties = d
        return user_update

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
