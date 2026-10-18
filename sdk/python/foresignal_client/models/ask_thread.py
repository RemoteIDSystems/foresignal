from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.ask_action import AskAction
    from ..models.ask_message import AskMessage


T = TypeVar("T", bound="AskThread")


@_attrs_define
class AskThread:
    """
    Attributes:
        id (UUID):
        title (str):
        created_at (datetime.datetime):
        updated_at (datetime.datetime | Unset):
        messages (list[AskMessage] | Unset):
        actions (list[AskAction] | Unset):
    """

    id: UUID
    title: str
    created_at: datetime.datetime
    updated_at: datetime.datetime | Unset = UNSET
    messages: list[AskMessage] | Unset = UNSET
    actions: list[AskAction] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = str(self.id)

        title = self.title

        created_at = self.created_at.isoformat()

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

        messages: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.messages, Unset):
            messages = []
            for messages_item_data in self.messages:
                messages_item = messages_item_data.to_dict()
                messages.append(messages_item)

        actions: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.actions, Unset):
            actions = []
            for actions_item_data in self.actions:
                actions_item = actions_item_data.to_dict()
                actions.append(actions_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "title": title,
                "createdAt": created_at,
            }
        )
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at
        if messages is not UNSET:
            field_dict["messages"] = messages
        if actions is not UNSET:
            field_dict["actions"] = actions

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ask_action import AskAction
        from ..models.ask_message import AskMessage

        d = dict(src_dict)
        id = UUID(d.pop("id"))

        title = d.pop("title")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

        _messages = d.pop("messages", UNSET)
        messages: list[AskMessage] | Unset = UNSET
        if _messages is not UNSET:
            messages = []
            for messages_item_data in _messages:
                messages_item = AskMessage.from_dict(messages_item_data)

                messages.append(messages_item)

        _actions = d.pop("actions", UNSET)
        actions: list[AskAction] | Unset = UNSET
        if _actions is not UNSET:
            actions = []
            for actions_item_data in _actions:
                actions_item = AskAction.from_dict(actions_item_data)

                actions.append(actions_item)

        ask_thread = cls(
            id=id,
            title=title,
            created_at=created_at,
            updated_at=updated_at,
            messages=messages,
            actions=actions,
        )

        ask_thread.additional_properties = d
        return ask_thread

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
