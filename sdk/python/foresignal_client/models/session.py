from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="Session")


@_attrs_define
class Session:
    """An active session of the calling user (user_session, 166)

    Attributes:
        id (UUID):
        created_at (datetime.datetime):
        expires_at (datetime.datetime):
        current (bool): True for the session making this request
        token_prefix (str | Unset):
        last_seen_at (datetime.datetime | None | Unset):
        idle_expires_at (datetime.datetime | None | Unset):
        user_agent (None | str | Unset):
        ip_address (None | str | Unset): Source IP at creation
    """

    id: UUID
    created_at: datetime.datetime
    expires_at: datetime.datetime
    current: bool
    token_prefix: str | Unset = UNSET
    last_seen_at: datetime.datetime | None | Unset = UNSET
    idle_expires_at: datetime.datetime | None | Unset = UNSET
    user_agent: None | str | Unset = UNSET
    ip_address: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = str(self.id)

        created_at = self.created_at.isoformat()

        expires_at = self.expires_at.isoformat()

        current = self.current

        token_prefix = self.token_prefix

        last_seen_at: None | str | Unset
        if isinstance(self.last_seen_at, Unset):
            last_seen_at = UNSET
        elif isinstance(self.last_seen_at, datetime.datetime):
            last_seen_at = self.last_seen_at.isoformat()
        else:
            last_seen_at = self.last_seen_at

        idle_expires_at: None | str | Unset
        if isinstance(self.idle_expires_at, Unset):
            idle_expires_at = UNSET
        elif isinstance(self.idle_expires_at, datetime.datetime):
            idle_expires_at = self.idle_expires_at.isoformat()
        else:
            idle_expires_at = self.idle_expires_at

        user_agent: None | str | Unset
        if isinstance(self.user_agent, Unset):
            user_agent = UNSET
        else:
            user_agent = self.user_agent

        ip_address: None | str | Unset
        if isinstance(self.ip_address, Unset):
            ip_address = UNSET
        else:
            ip_address = self.ip_address

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "createdAt": created_at,
                "expiresAt": expires_at,
                "current": current,
            }
        )
        if token_prefix is not UNSET:
            field_dict["tokenPrefix"] = token_prefix
        if last_seen_at is not UNSET:
            field_dict["lastSeenAt"] = last_seen_at
        if idle_expires_at is not UNSET:
            field_dict["idleExpiresAt"] = idle_expires_at
        if user_agent is not UNSET:
            field_dict["userAgent"] = user_agent
        if ip_address is not UNSET:
            field_dict["ipAddress"] = ip_address

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = UUID(d.pop("id"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        expires_at = datetime.datetime.fromisoformat(d.pop("expiresAt"))

        current = d.pop("current")

        token_prefix = d.pop("tokenPrefix", UNSET)

        def _parse_last_seen_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_seen_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_seen_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_seen_at = _parse_last_seen_at(d.pop("lastSeenAt", UNSET))

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

        def _parse_user_agent(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        user_agent = _parse_user_agent(d.pop("userAgent", UNSET))

        def _parse_ip_address(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        ip_address = _parse_ip_address(d.pop("ipAddress", UNSET))

        session = cls(
            id=id,
            created_at=created_at,
            expires_at=expires_at,
            current=current,
            token_prefix=token_prefix,
            last_seen_at=last_seen_at,
            idle_expires_at=idle_expires_at,
            user_agent=user_agent,
            ip_address=ip_address,
        )

        session.additional_properties = d
        return session

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
