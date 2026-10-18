from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.allowed_chat_scope import AllowedChatScope
from ..types import UNSET, Unset

T = TypeVar("T", bound="AllowedChat")


@_attrs_define
class AllowedChat:
    """One allowlisted chat (Telegram chat id or Matrix room id) and its scope (061). Groups and rooms with more than two
    members must list `userIds`.

        Attributes:
            chat_id (str): Telegram numeric chat id as a string (negative for groups) or Matrix `!room:server` Example:
                -1001234567890.
            scope (AllowedChatScope): `read`: /help, /status, /alerts, /alert, /ask (proposed actions listed, not
                executable), /brief, /sources. `agent`: also /ack, /resolve, /snooze, /brief run and confirming proposed
                actions. Checked against the same AccessPolicy rule as the REST operation each command mirrors.
            name (str | Unset):
            user_ids (list[str] | Unset): Allowed senders (Telegram user ids or Matrix MXIDs)
            private_access (bool | Unset): The 039 private grant: the chat may see private sources (Ask still follows the
                LLM egress rules) Default: False.
    """

    chat_id: str
    scope: AllowedChatScope
    name: str | Unset = UNSET
    user_ids: list[str] | Unset = UNSET
    private_access: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        chat_id = self.chat_id

        scope = self.scope.value

        name = self.name

        user_ids: list[str] | Unset = UNSET
        if not isinstance(self.user_ids, Unset):
            user_ids = self.user_ids

        private_access = self.private_access

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "chatId": chat_id,
                "scope": scope,
            }
        )
        if name is not UNSET:
            field_dict["name"] = name
        if user_ids is not UNSET:
            field_dict["userIds"] = user_ids
        if private_access is not UNSET:
            field_dict["privateAccess"] = private_access

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        chat_id = d.pop("chatId")

        scope = AllowedChatScope(d.pop("scope"))

        name = d.pop("name", UNSET)

        user_ids = cast(list[str], d.pop("userIds", UNSET))

        private_access = d.pop("privateAccess", UNSET)

        allowed_chat = cls(
            chat_id=chat_id,
            scope=scope,
            name=name,
            user_ids=user_ids,
            private_access=private_access,
        )

        allowed_chat.additional_properties = d
        return allowed_chat

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
