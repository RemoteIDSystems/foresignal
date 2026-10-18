from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.object_acl_entry_role import ObjectAclEntryRole
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.principal_summary import PrincipalSummary


T = TypeVar("T", bound="ObjectAclEntry")


@_attrs_define
class ObjectAclEntry:
    """One access-list grant

    Attributes:
        grantee (str): `principal:{id}`, `group:{id}` or `instance` Example: group:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        role (ObjectAclEntryRole):
        granted_at (datetime.datetime):
        grantee_display (None | str | Unset): Handle or group name for display
        granted_by (PrincipalSummary | Unset): Public view of a principal: handle, display name, kind and avatar only
        expires_at (datetime.datetime | None | Unset):
    """

    grantee: str
    role: ObjectAclEntryRole
    granted_at: datetime.datetime
    grantee_display: None | str | Unset = UNSET
    granted_by: PrincipalSummary | Unset = UNSET
    expires_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        grantee = self.grantee

        role = self.role.value

        granted_at = self.granted_at.isoformat()

        grantee_display: None | str | Unset
        if isinstance(self.grantee_display, Unset):
            grantee_display = UNSET
        else:
            grantee_display = self.grantee_display

        granted_by: dict[str, Any] | Unset = UNSET
        if not isinstance(self.granted_by, Unset):
            granted_by = self.granted_by.to_dict()

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
                "grantedAt": granted_at,
            }
        )
        if grantee_display is not UNSET:
            field_dict["granteeDisplay"] = grantee_display
        if granted_by is not UNSET:
            field_dict["grantedBy"] = granted_by
        if expires_at is not UNSET:
            field_dict["expiresAt"] = expires_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.principal_summary import PrincipalSummary

        d = dict(src_dict)
        grantee = d.pop("grantee")

        role = ObjectAclEntryRole(d.pop("role"))

        granted_at = datetime.datetime.fromisoformat(d.pop("grantedAt"))

        def _parse_grantee_display(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        grantee_display = _parse_grantee_display(d.pop("granteeDisplay", UNSET))

        _granted_by = d.pop("grantedBy", UNSET)
        granted_by: PrincipalSummary | Unset
        if isinstance(_granted_by, Unset):
            granted_by = UNSET
        else:
            granted_by = PrincipalSummary.from_dict(_granted_by)

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

        object_acl_entry = cls(
            grantee=grantee,
            role=role,
            granted_at=granted_at,
            grantee_display=grantee_display,
            granted_by=granted_by,
            expires_at=expires_at,
        )

        object_acl_entry.additional_properties = d
        return object_acl_entry

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
