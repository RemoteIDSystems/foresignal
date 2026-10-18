from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.health_status import HealthStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.channel_interactive_status_chats_item import (
        ChannelInteractiveStatusChatsItem,
    )


T = TypeVar("T", bound="ChannelInteractiveStatus")


@_attrs_define
class ChannelInteractiveStatus:
    """State of an interactive chat channel's connector (061); also embedded as `Channel.interactive`

    Attributes:
        interactive (bool):
        connected (bool):
        status (HealthStatus):
        chats (list[ChannelInteractiveStatusChatsItem]):
        chatops_enabled (bool | Unset): Global `chatops.enabled` kill switch
        status_reason (None | str | Unset):
        last_poll_at (datetime.datetime | None | Unset):
        last_error (None | str | Unset):
        discovery_mode_until (datetime.datetime | None | Unset):
    """

    interactive: bool
    connected: bool
    status: HealthStatus
    chats: list[ChannelInteractiveStatusChatsItem]
    chatops_enabled: bool | Unset = UNSET
    status_reason: None | str | Unset = UNSET
    last_poll_at: datetime.datetime | None | Unset = UNSET
    last_error: None | str | Unset = UNSET
    discovery_mode_until: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        interactive = self.interactive

        connected = self.connected

        status = self.status.value

        chats = []
        for chats_item_data in self.chats:
            chats_item = chats_item_data.to_dict()
            chats.append(chats_item)

        chatops_enabled = self.chatops_enabled

        status_reason: None | str | Unset
        if isinstance(self.status_reason, Unset):
            status_reason = UNSET
        else:
            status_reason = self.status_reason

        last_poll_at: None | str | Unset
        if isinstance(self.last_poll_at, Unset):
            last_poll_at = UNSET
        elif isinstance(self.last_poll_at, datetime.datetime):
            last_poll_at = self.last_poll_at.isoformat()
        else:
            last_poll_at = self.last_poll_at

        last_error: None | str | Unset
        if isinstance(self.last_error, Unset):
            last_error = UNSET
        else:
            last_error = self.last_error

        discovery_mode_until: None | str | Unset
        if isinstance(self.discovery_mode_until, Unset):
            discovery_mode_until = UNSET
        elif isinstance(self.discovery_mode_until, datetime.datetime):
            discovery_mode_until = self.discovery_mode_until.isoformat()
        else:
            discovery_mode_until = self.discovery_mode_until

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "interactive": interactive,
                "connected": connected,
                "status": status,
                "chats": chats,
            }
        )
        if chatops_enabled is not UNSET:
            field_dict["chatopsEnabled"] = chatops_enabled
        if status_reason is not UNSET:
            field_dict["statusReason"] = status_reason
        if last_poll_at is not UNSET:
            field_dict["lastPollAt"] = last_poll_at
        if last_error is not UNSET:
            field_dict["lastError"] = last_error
        if discovery_mode_until is not UNSET:
            field_dict["discoveryModeUntil"] = discovery_mode_until

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.channel_interactive_status_chats_item import (
            ChannelInteractiveStatusChatsItem,
        )

        d = dict(src_dict)
        interactive = d.pop("interactive")

        connected = d.pop("connected")

        status = HealthStatus(d.pop("status"))

        chats = []
        _chats = d.pop("chats")
        for chats_item_data in _chats:
            chats_item = ChannelInteractiveStatusChatsItem.from_dict(chats_item_data)

            chats.append(chats_item)

        chatops_enabled = d.pop("chatopsEnabled", UNSET)

        def _parse_status_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        status_reason = _parse_status_reason(d.pop("statusReason", UNSET))

        def _parse_last_poll_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_poll_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_poll_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_poll_at = _parse_last_poll_at(d.pop("lastPollAt", UNSET))

        def _parse_last_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        last_error = _parse_last_error(d.pop("lastError", UNSET))

        def _parse_discovery_mode_until(
            data: object,
        ) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                discovery_mode_until_type_0 = datetime.datetime.fromisoformat(data)

                return discovery_mode_until_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        discovery_mode_until = _parse_discovery_mode_until(
            d.pop("discoveryModeUntil", UNSET)
        )

        channel_interactive_status = cls(
            interactive=interactive,
            connected=connected,
            status=status,
            chats=chats,
            chatops_enabled=chatops_enabled,
            status_reason=status_reason,
            last_poll_at=last_poll_at,
            last_error=last_error,
            discovery_mode_until=discovery_mode_until,
        )

        channel_interactive_status.additional_properties = d
        return channel_interactive_status

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
