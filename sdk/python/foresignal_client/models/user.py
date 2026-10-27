from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.role_ref import RoleRef
    from ..models.user_data_restriction import UserDataRestriction
    from ..models.user_link_issued import UserLinkIssued


T = TypeVar("T", bound="User")


@_attrs_define
class User:
    """A platform user (user table, 164)

    Attributes:
        id (UUID):
        email (str): Unique, normalized; the primary identity
        full_name (str):
        status (str):
        roles (list[RoleRef]): Assigned roles; effective permissions are their union
        mfa_enabled (bool | Unset):  Default: False.
        created_by (None | Unset | UUID): Admin who created the account; null for the bootstrap first admin
        created_at (datetime.datetime | Unset):
        updated_at (datetime.datetime | Unset):
        last_login_at (datetime.datetime | None | Unset):
        activation (UserLinkIssued | Unset): A one-time activation or reset link issued for a user (R2-005). When the
            email was not sent (no SMTP, or `sendInvite: false`) the plaintext `token` is returned here once for the admin
            to pass on; when it was emailed, `token` is null. Only the token's hash is stored.
        data_restriction (None | Unset | UserDataRestriction): The user's data restriction, or null when unrestricted
            (R2-013)
    """

    id: UUID
    email: str
    full_name: str
    status: str
    roles: list[RoleRef]
    mfa_enabled: bool | Unset = False
    created_by: None | Unset | UUID = UNSET
    created_at: datetime.datetime | Unset = UNSET
    updated_at: datetime.datetime | Unset = UNSET
    last_login_at: datetime.datetime | None | Unset = UNSET
    activation: UserLinkIssued | Unset = UNSET
    data_restriction: None | Unset | UserDataRestriction = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.user_data_restriction import UserDataRestriction

        id = str(self.id)

        email = self.email

        full_name = self.full_name

        status = self.status

        roles = []
        for roles_item_data in self.roles:
            roles_item = roles_item_data.to_dict()
            roles.append(roles_item)

        mfa_enabled = self.mfa_enabled

        created_by: None | str | Unset
        if isinstance(self.created_by, Unset):
            created_by = UNSET
        elif isinstance(self.created_by, UUID):
            created_by = str(self.created_by)
        else:
            created_by = self.created_by

        created_at: str | Unset = UNSET
        if not isinstance(self.created_at, Unset):
            created_at = self.created_at.isoformat()

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

        last_login_at: None | str | Unset
        if isinstance(self.last_login_at, Unset):
            last_login_at = UNSET
        elif isinstance(self.last_login_at, datetime.datetime):
            last_login_at = self.last_login_at.isoformat()
        else:
            last_login_at = self.last_login_at

        activation: dict[str, Any] | Unset = UNSET
        if not isinstance(self.activation, Unset):
            activation = self.activation.to_dict()

        data_restriction: dict[str, Any] | None | Unset
        if isinstance(self.data_restriction, Unset):
            data_restriction = UNSET
        elif isinstance(self.data_restriction, UserDataRestriction):
            data_restriction = self.data_restriction.to_dict()
        else:
            data_restriction = self.data_restriction

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "email": email,
                "fullName": full_name,
                "status": status,
                "roles": roles,
            }
        )
        if mfa_enabled is not UNSET:
            field_dict["mfaEnabled"] = mfa_enabled
        if created_by is not UNSET:
            field_dict["createdBy"] = created_by
        if created_at is not UNSET:
            field_dict["createdAt"] = created_at
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at
        if last_login_at is not UNSET:
            field_dict["lastLoginAt"] = last_login_at
        if activation is not UNSET:
            field_dict["activation"] = activation
        if data_restriction is not UNSET:
            field_dict["dataRestriction"] = data_restriction

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.role_ref import RoleRef
        from ..models.user_data_restriction import UserDataRestriction
        from ..models.user_link_issued import UserLinkIssued

        d = dict(src_dict)
        id = UUID(d.pop("id"))

        email = d.pop("email")

        full_name = d.pop("fullName")

        status = d.pop("status")

        roles = []
        _roles = d.pop("roles")
        for roles_item_data in _roles:
            roles_item = RoleRef.from_dict(roles_item_data)

            roles.append(roles_item)

        mfa_enabled = d.pop("mfaEnabled", UNSET)

        def _parse_created_by(data: object) -> None | Unset | UUID:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                created_by_type_0 = UUID(data)

                return created_by_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Unset | UUID, data)

        created_by = _parse_created_by(d.pop("createdBy", UNSET))

        _created_at = d.pop("createdAt", UNSET)
        created_at: datetime.datetime | Unset
        if isinstance(_created_at, Unset):
            created_at = UNSET
        else:
            created_at = datetime.datetime.fromisoformat(_created_at)

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

        def _parse_last_login_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_login_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_login_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_login_at = _parse_last_login_at(d.pop("lastLoginAt", UNSET))

        _activation = d.pop("activation", UNSET)
        activation: UserLinkIssued | Unset
        if isinstance(_activation, Unset):
            activation = UNSET
        else:
            activation = UserLinkIssued.from_dict(_activation)

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

        user = cls(
            id=id,
            email=email,
            full_name=full_name,
            status=status,
            roles=roles,
            mfa_enabled=mfa_enabled,
            created_by=created_by,
            created_at=created_at,
            updated_at=updated_at,
            last_login_at=last_login_at,
            activation=activation,
            data_restriction=data_restriction,
        )

        user.additional_properties = d
        return user

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
