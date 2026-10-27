from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="UserCreate")


@_attrs_define
class UserCreate:
    """Create (invite) a user; a welcome/activation email is sent when SMTP is configured (170)

    Attributes:
        email (str):
        full_name (str):
        roles (list[UUID]): Role ids to assign on creation
        send_invite (bool | Unset): Email an activation link; when false an admin retrieves the link Default: True.
    """

    email: str
    full_name: str
    roles: list[UUID]
    send_invite: bool | Unset = True
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        email = self.email

        full_name = self.full_name

        roles = []
        for roles_item_data in self.roles:
            roles_item = str(roles_item_data)
            roles.append(roles_item)

        send_invite = self.send_invite

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "email": email,
                "fullName": full_name,
                "roles": roles,
            }
        )
        if send_invite is not UNSET:
            field_dict["sendInvite"] = send_invite

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        email = d.pop("email")

        full_name = d.pop("fullName")

        roles = []
        _roles = d.pop("roles")
        for roles_item_data in _roles:
            roles_item = UUID(roles_item_data)

            roles.append(roles_item)

        send_invite = d.pop("sendInvite", UNSET)

        user_create = cls(
            email=email,
            full_name=full_name,
            roles=roles,
            send_invite=send_invite,
        )

        user_create.additional_properties = d
        return user_create

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
