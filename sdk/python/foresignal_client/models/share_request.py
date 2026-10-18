from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.share_request_role import ShareRequestRole
from ..types import UNSET, Unset

T = TypeVar("T", bound="ShareRequest")


@_attrs_define
class ShareRequest:
    """Share an object with principals, groups or the instance; grantees are notified

    Attributes:
        grantees (list[str]):
        role (ShareRequestRole | Unset):  Default: ShareRequestRole.VIEWER.
        message (None | str | Unset):
        expires_at (datetime.datetime | None | Unset):
    """

    grantees: list[str]
    role: ShareRequestRole | Unset = ShareRequestRole.VIEWER
    message: None | str | Unset = UNSET
    expires_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        grantees = self.grantees

        role: str | Unset = UNSET
        if not isinstance(self.role, Unset):
            role = self.role.value

        message: None | str | Unset
        if isinstance(self.message, Unset):
            message = UNSET
        else:
            message = self.message

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
                "grantees": grantees,
            }
        )
        if role is not UNSET:
            field_dict["role"] = role
        if message is not UNSET:
            field_dict["message"] = message
        if expires_at is not UNSET:
            field_dict["expiresAt"] = expires_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        grantees = cast(list[str], d.pop("grantees"))

        _role = d.pop("role", UNSET)
        role: ShareRequestRole | Unset
        if isinstance(_role, Unset):
            role = UNSET
        else:
            role = ShareRequestRole(_role)

        def _parse_message(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        message = _parse_message(d.pop("message", UNSET))

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

        share_request = cls(
            grantees=grantees,
            role=role,
            message=message,
            expires_at=expires_at,
        )

        share_request.additional_properties = d
        return share_request

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
