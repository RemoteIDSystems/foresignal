from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.push_payload_actions_item_action import PushPayloadActionsItemAction

T = TypeVar("T", bound="PushPayloadActionsItem")


@_attrs_define
class PushPayloadActionsItem:
    """Action button (033)

    Attributes:
        action (PushPayloadActionsItemAction):
        title (str):
        url (str):
    """

    action: PushPayloadActionsItemAction
    title: str
    url: str
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        action = self.action.value

        title = self.title

        url = self.url

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "action": action,
                "title": title,
                "url": url,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        action = PushPayloadActionsItemAction(d.pop("action"))

        title = d.pop("title")

        url = d.pop("url")

        push_payload_actions_item = cls(
            action=action,
            title=title,
            url=url,
        )

        push_payload_actions_item.additional_properties = d
        return push_payload_actions_item

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
