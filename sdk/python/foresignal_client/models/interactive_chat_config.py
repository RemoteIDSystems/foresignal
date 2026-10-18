from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.allowed_chat import AllowedChat


T = TypeVar("T", bound="InteractiveChatConfig")


@_attrs_define
class InteractiveChatConfig:
    """Interactive-command fields added to `ChannelInput.config` for types `telegram` and `matrix` (061). Global kill
    switch: `NotificationSettings.chatops.enabled`.

        Attributes:
            interactive (bool | Unset): Accept commands from `allowedChats` (`/alerts`, `/alert`, `/ack`, `/resolve`,
                `/snooze`, `/ask`, `/brief`, `/status`, `/sources`, `/help`, plus `/confirm` and `/reject` codes on Matrix).
                Telegram needs a bot used only by this Foresignal instance; Matrix rooms must be unencrypted. Default: False.
            allowed_chats (list[AllowedChat] | Unset): The chats that may command, each mapped to `read` or `agent` scope
                (never `admin`). Messages from any other chat or user are ignored silently. Telegram groups (negative ids) must
                list `userIds`; a Matrix room with more than two members and no `userIds` is ignored when the connector starts.
            ask_rate_limit_per_hour (int | Unset): `/ask` questions per chat per hour (0 turns `/ask` off for chats); the
                LLM budget applies as well Default: 10.
            command_rate_limit (int | Unset): Commands per chat per 10 minutes Default: 30.
            take_over_webhook (bool | Unset): Telegram: delete a foreign webhook at start Default: False.
            discovery_mode (bool | Unset): `/whoami` answers any chat with its ids; auto-off after 10 minutes Default:
                False.
    """

    interactive: bool | Unset = False
    allowed_chats: list[AllowedChat] | Unset = UNSET
    ask_rate_limit_per_hour: int | Unset = 10
    command_rate_limit: int | Unset = 30
    take_over_webhook: bool | Unset = False
    discovery_mode: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        interactive = self.interactive

        allowed_chats: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.allowed_chats, Unset):
            allowed_chats = []
            for allowed_chats_item_data in self.allowed_chats:
                allowed_chats_item = allowed_chats_item_data.to_dict()
                allowed_chats.append(allowed_chats_item)

        ask_rate_limit_per_hour = self.ask_rate_limit_per_hour

        command_rate_limit = self.command_rate_limit

        take_over_webhook = self.take_over_webhook

        discovery_mode = self.discovery_mode

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if interactive is not UNSET:
            field_dict["interactive"] = interactive
        if allowed_chats is not UNSET:
            field_dict["allowedChats"] = allowed_chats
        if ask_rate_limit_per_hour is not UNSET:
            field_dict["askRateLimitPerHour"] = ask_rate_limit_per_hour
        if command_rate_limit is not UNSET:
            field_dict["commandRateLimit"] = command_rate_limit
        if take_over_webhook is not UNSET:
            field_dict["takeOverWebhook"] = take_over_webhook
        if discovery_mode is not UNSET:
            field_dict["discoveryMode"] = discovery_mode

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.allowed_chat import AllowedChat

        d = dict(src_dict)
        interactive = d.pop("interactive", UNSET)

        _allowed_chats = d.pop("allowedChats", UNSET)
        allowed_chats: list[AllowedChat] | Unset = UNSET
        if _allowed_chats is not UNSET:
            allowed_chats = []
            for allowed_chats_item_data in _allowed_chats:
                allowed_chats_item = AllowedChat.from_dict(allowed_chats_item_data)

                allowed_chats.append(allowed_chats_item)

        ask_rate_limit_per_hour = d.pop("askRateLimitPerHour", UNSET)

        command_rate_limit = d.pop("commandRateLimit", UNSET)

        take_over_webhook = d.pop("takeOverWebhook", UNSET)

        discovery_mode = d.pop("discoveryMode", UNSET)

        interactive_chat_config = cls(
            interactive=interactive,
            allowed_chats=allowed_chats,
            ask_rate_limit_per_hour=ask_rate_limit_per_hour,
            command_rate_limit=command_rate_limit,
            take_over_webhook=take_over_webhook,
            discovery_mode=discovery_mode,
        )

        interactive_chat_config.additional_properties = d
        return interactive_chat_config

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
