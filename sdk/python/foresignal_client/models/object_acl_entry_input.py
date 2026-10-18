from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.object_acl_entry_input_role import ObjectAclEntryInputRole
from ..types import UNSET, Unset

T = TypeVar("T", bound="ObjectAclEntryInput")


@_attrs_define
class ObjectAclEntryInput:
    """One access-list grant to set

    Attributes:
        grantee (str): `principal:{id}`, `group:{id}` or `instance` Example: group:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        role (ObjectAclEntryInputRole):
        expires_at (datetime.datetime | None | Unset):
    """

    grantee: str
    role: ObjectAclEntryInputRole
    expires_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        grantee = self.grantee

        role = self.role.value

        expires_at: None | str | Unset
        if isinstance(self.expires_at, Unset):
            expires_at = UNSET
        elif isinstance(self.expires_at, datetime.datetime):
            expires_at = self.expires_at.isoformat()
        else:
            expires_at = self.expires_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "grantee": grantee,
                "role": role,
            }
        )
        if expires_at is not UNSET:
            field_dict["expiresAt"] = expires_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        grantee = d.pop("grantee")

        role = ObjectAclEntryInputRole(d.pop("role"))

        def _parse_expires_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                expires_at_type_0 = datetime.datetime.fromisoformat(data)

                return expires_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        expires_at = _parse_expires_at(d.pop("expiresAt", UNSET))

        object_acl_entry_input = cls(
            grantee=grantee,
            role=role,
            expires_at=expires_at,
        )

        object_acl_entry_input.additional_properties = d
        return object_acl_entry_input

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
