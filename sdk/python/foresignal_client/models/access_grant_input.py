from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.access_grant_input_deliver_as import AccessGrantInputDeliverAs
from ..models.access_grant_input_role import AccessGrantInputRole
from ..types import UNSET, Unset

T = TypeVar("T", bound="AccessGrantInput")


@_attrs_define
class AccessGrantInput:
    """Grant an access request (owner)

    Attributes:
        role (AccessGrantInputRole | Unset):  Default: AccessGrantInputRole.VIEWER.
        expires_at (datetime.datetime | None | Unset):
        deliver_as (AccessGrantInputDeliverAs | Unset): For remote requesters: a 132 package Default:
            AccessGrantInputDeliverAs.CHRONICLE_EXTRACT.
        message (None | str | Unset):
    """

    role: AccessGrantInputRole | Unset = AccessGrantInputRole.VIEWER
    expires_at: datetime.datetime | None | Unset = UNSET
    deliver_as: AccessGrantInputDeliverAs | Unset = (
        AccessGrantInputDeliverAs.CHRONICLE_EXTRACT
    )
    message: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        role: str | Unset = UNSET
        if not isinstance(self.role, Unset):
            role = self.role.value

        expires_at: None | str | Unset
        if isinstance(self.expires_at, Unset):
            expires_at = UNSET
        elif isinstance(self.expires_at, datetime.datetime):
            expires_at = self.expires_at.isoformat()
        else:
            expires_at = self.expires_at

        deliver_as: str | Unset = UNSET
        if not isinstance(self.deliver_as, Unset):
            deliver_as = self.deliver_as.value

        message: None | str | Unset
        if isinstance(self.message, Unset):
            message = UNSET
        else:
            message = self.message

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if role is not UNSET:
            field_dict["role"] = role
        if expires_at is not UNSET:
            field_dict["expiresAt"] = expires_at
        if deliver_as is not UNSET:
            field_dict["deliverAs"] = deliver_as
        if message is not UNSET:
            field_dict["message"] = message

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        _role = d.pop("role", UNSET)
        role: AccessGrantInputRole | Unset
        if isinstance(_role, Unset):
            role = UNSET
        else:
            role = AccessGrantInputRole(_role)

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

        _deliver_as = d.pop("deliverAs", UNSET)
        deliver_as: AccessGrantInputDeliverAs | Unset
        if isinstance(_deliver_as, Unset):
            deliver_as = UNSET
        else:
            deliver_as = AccessGrantInputDeliverAs(_deliver_as)

        def _parse_message(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        message = _parse_message(d.pop("message", UNSET))

        access_grant_input = cls(
            role=role,
            expires_at=expires_at,
            deliver_as=deliver_as,
            message=message,
        )

        access_grant_input.additional_properties = d
        return access_grant_input

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
