from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.channel_interactive_status_chats_item_scope import (
    ChannelInteractiveStatusChatsItemScope,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="ChannelInteractiveStatusChatsItem")


@_attrs_define
class ChannelInteractiveStatusChatsItem:
    """One allowlisted chat and whether it is served

    Attributes:
        chat_id (str):
        scope (ChannelInteractiveStatusChatsItemScope):
        active (bool): false when ignored (e.g. an encrypted Matrix room)
        name (None | str | Unset):
        user_ids (list[str] | Unset):
        private_access (bool | Unset):
        reason (None | str | Unset):
    """

    chat_id: str
    scope: ChannelInteractiveStatusChatsItemScope
    active: bool
    name: None | str | Unset = UNSET
    user_ids: list[str] | Unset = UNSET
    private_access: bool | Unset = UNSET
    reason: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        chat_id = self.chat_id

        scope = self.scope.value

        active = self.active

        name: None | str | Unset
        if isinstance(self.name, Unset):
            name = UNSET
        else:
            name = self.name

        user_ids: list[str] | Unset = UNSET
        if not isinstance(self.user_ids, Unset):
            user_ids = self.user_ids

        private_access = self.private_access

        reason: None | str | Unset
        if isinstance(self.reason, Unset):
            reason = UNSET
        else:
            reason = self.reason

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "chatId": chat_id,
                "scope": scope,
                "active": active,
            }
        )
        if name is not UNSET:
            field_dict["name"] = name
        if user_ids is not UNSET:
            field_dict["userIds"] = user_ids
        if private_access is not UNSET:
            field_dict["privateAccess"] = private_access
        if reason is not UNSET:
            field_dict["reason"] = reason

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        chat_id = d.pop("chatId")

        scope = ChannelInteractiveStatusChatsItemScope(d.pop("scope"))

        active = d.pop("active")

        def _parse_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        name = _parse_name(d.pop("name", UNSET))

        user_ids = cast(list[str], d.pop("userIds", UNSET))

        private_access = d.pop("privateAccess", UNSET)

        def _parse_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        reason = _parse_reason(d.pop("reason", UNSET))

        channel_interactive_status_chats_item = cls(
            chat_id=chat_id,
            scope=scope,
            active=active,
            name=name,
            user_ids=user_ids,
            private_access=private_access,
            reason=reason,
        )

        channel_interactive_status_chats_item.additional_properties = d
        return channel_interactive_status_chats_item

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
