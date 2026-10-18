from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="NtfyPublishRequest")


@_attrs_define
class NtfyPublishRequest:
    """ntfy JSON publish body (`POST /compat/ntfy`, used by Uptime Kuma) (024)

    Attributes:
        topic (str): The custom source id
        message (str | Unset):
        title (str | Unset):
        priority (int | Unset):
        tags (list[str] | Unset):
        click (str | Unset):
    """

    topic: str
    message: str | Unset = UNSET
    title: str | Unset = UNSET
    priority: int | Unset = UNSET
    tags: list[str] | Unset = UNSET
    click: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        topic = self.topic

        message = self.message

        title = self.title

        priority = self.priority

        tags: list[str] | Unset = UNSET
        if not isinstance(self.tags, Unset):
            tags = self.tags

        click = self.click

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "topic": topic,
            }
        )
        if message is not UNSET:
            field_dict["message"] = message
        if title is not UNSET:
            field_dict["title"] = title
        if priority is not UNSET:
            field_dict["priority"] = priority
        if tags is not UNSET:
            field_dict["tags"] = tags
        if click is not UNSET:
            field_dict["click"] = click

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        topic = d.pop("topic")

        message = d.pop("message", UNSET)

        title = d.pop("title", UNSET)

        priority = d.pop("priority", UNSET)

        tags = cast(list[str], d.pop("tags", UNSET))

        click = d.pop("click", UNSET)

        ntfy_publish_request = cls(
            topic=topic,
            message=message,
            title=title,
            priority=priority,
            tags=tags,
            click=click,
        )

        ntfy_publish_request.additional_properties = d
        return ntfy_publish_request

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
