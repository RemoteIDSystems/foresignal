from __future__ import annotations

import json
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from .. import types
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.gotify_message_input_extras import GotifyMessageInputExtras


T = TypeVar("T", bound="GotifyMessageInput")


@_attrs_define
class GotifyMessageInput:
    """Gotify message body (JSON or form) accepted by `POST /compat/gotify/message` (024)

    Attributes:
        message (str):
        title (str | Unset):
        priority (int | Unset):
        extras (GotifyMessageInputExtras | Unset): `extras["foresignal::record"].externalId` sets the externalId
    """

    message: str
    title: str | Unset = UNSET
    priority: int | Unset = UNSET
    extras: GotifyMessageInputExtras | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        message = self.message

        title = self.title

        priority = self.priority

        extras: dict[str, Any] | Unset = UNSET
        if not isinstance(self.extras, Unset):
            extras = self.extras.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "message": message,
            }
        )
        if title is not UNSET:
            field_dict["title"] = title
        if priority is not UNSET:
            field_dict["priority"] = priority
        if extras is not UNSET:
            field_dict["extras"] = extras

        return field_dict

    def to_multipart(self) -> types.RequestFiles:
        files: types.RequestFiles = []

        files.append(("message", (None, str(self.message).encode(), "text/plain")))

        if not isinstance(self.title, Unset):
            files.append(("title", (None, str(self.title).encode(), "text/plain")))

        if not isinstance(self.priority, Unset):
            files.append(
                ("priority", (None, str(self.priority).encode(), "text/plain"))
            )

        if not isinstance(self.extras, Unset):
            files.append(
                (
                    "extras",
                    (
                        None,
                        json.dumps(self.extras.to_dict()).encode(),
                        "application/json",
                    ),
                )
            )

        for prop_name, prop in self.additional_properties.items():
            files.append((prop_name, (None, str(prop).encode(), "text/plain")))

        return files

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.gotify_message_input_extras import GotifyMessageInputExtras

        d = dict(src_dict)
        message = d.pop("message")

        title = d.pop("title", UNSET)

        priority = d.pop("priority", UNSET)

        _extras = d.pop("extras", UNSET)
        extras: GotifyMessageInputExtras | Unset
        if isinstance(_extras, Unset):
            extras = UNSET
        else:
            extras = GotifyMessageInputExtras.from_dict(_extras)

        gotify_message_input = cls(
            message=message,
            title=title,
            priority=priority,
            extras=extras,
        )

        gotify_message_input.additional_properties = d
        return gotify_message_input

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
