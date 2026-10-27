from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SessionToken")


@_attrs_define
class SessionToken:
    """A newly issued session; the token is a bearer carried in the Authorization header on later requests (Argon2id-
    verified login, 166)

        Attributes:
            token (str): The session bearer; shown once, stored hashed server-side. Empty while mfaRequired: no session was
                issued (R2-024)
            user_id (UUID):
            expires_at (datetime.datetime | None): Absolute expiry; null while mfaRequired (no session was issued, R2-024)
            token_prefix (str | Unset): First characters, for display in a session list
            issued_at (datetime.datetime | Unset):
            idle_expires_at (datetime.datetime | None | Unset): Idle expiry; refreshed on use
            mfa_required (bool | Unset): True when MFA is enrolled and a code must still be supplied to complete login: no
                session was issued, and the client signs in again with mfaCode (R2-024) Default: False.
            scopes (list[str] | Unset): Effective coarse scopes the session carries
            permissions (list[str] | Unset): Effective permission keys (union of the user's roles)
    """

    token: str
    user_id: UUID
    expires_at: datetime.datetime | None
    token_prefix: str | Unset = UNSET
    issued_at: datetime.datetime | Unset = UNSET
    idle_expires_at: datetime.datetime | None | Unset = UNSET
    mfa_required: bool | Unset = False
    scopes: list[str] | Unset = UNSET
    permissions: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        token = self.token

        user_id = str(self.user_id)

        expires_at: None | str
        if isinstance(self.expires_at, datetime.datetime):
            expires_at = self.expires_at.isoformat()
        else:
            expires_at = self.expires_at

        token_prefix = self.token_prefix

        issued_at: str | Unset = UNSET
        if not isinstance(self.issued_at, Unset):
            issued_at = self.issued_at.isoformat()

        idle_expires_at: None | str | Unset
        if isinstance(self.idle_expires_at, Unset):
            idle_expires_at = UNSET
        elif isinstance(self.idle_expires_at, datetime.datetime):
            idle_expires_at = self.idle_expires_at.isoformat()
        else:
            idle_expires_at = self.idle_expires_at

        mfa_required = self.mfa_required

        scopes: list[str] | Unset = UNSET
        if not isinstance(self.scopes, Unset):
            scopes = self.scopes

        permissions: list[str] | Unset = UNSET
        if not isinstance(self.permissions, Unset):
            permissions = self.permissions

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "token": token,
                "userId": user_id,
                "expiresAt": expires_at,
            }
        )
        if token_prefix is not UNSET:
            field_dict["tokenPrefix"] = token_prefix
        if issued_at is not UNSET:
            field_dict["issuedAt"] = issued_at
        if idle_expires_at is not UNSET:
            field_dict["idleExpiresAt"] = idle_expires_at
        if mfa_required is not UNSET:
            field_dict["mfaRequired"] = mfa_required
        if scopes is not UNSET:
            field_dict["scopes"] = scopes
        if permissions is not UNSET:
            field_dict["permissions"] = permissions

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        token = d.pop("token")

        user_id = UUID(d.pop("userId"))

        def _parse_expires_at(data: object) -> datetime.datetime | None:
            if data is None:
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                expires_at_type_0 = datetime.datetime.fromisoformat(data)

                return expires_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None, data)

        expires_at = _parse_expires_at(d.pop("expiresAt"))

        token_prefix = d.pop("tokenPrefix", UNSET)

        _issued_at = d.pop("issuedAt", UNSET)
        issued_at: datetime.datetime | Unset
        if isinstance(_issued_at, Unset):
            issued_at = UNSET
        else:
            issued_at = datetime.datetime.fromisoformat(_issued_at)

        def _parse_idle_expires_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                idle_expires_at_type_0 = datetime.datetime.fromisoformat(data)

                return idle_expires_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        idle_expires_at = _parse_idle_expires_at(d.pop("idleExpiresAt", UNSET))

        mfa_required = d.pop("mfaRequired", UNSET)

        scopes = cast(list[str], d.pop("scopes", UNSET))

        permissions = cast(list[str], d.pop("permissions", UNSET))

        session_token = cls(
            token=token,
            user_id=user_id,
            expires_at=expires_at,
            token_prefix=token_prefix,
            issued_at=issued_at,
            idle_expires_at=idle_expires_at,
            mfa_required=mfa_required,
            scopes=scopes,
            permissions=permissions,
        )

        session_token.additional_properties = d
        return session_token

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
