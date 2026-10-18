from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.gotify_message_appid import GotifyMessageAppid
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.gotify_message_extras import GotifyMessageExtras


T = TypeVar("T", bound="GotifyMessage")


@_attrs_define
class GotifyMessage:
    """Gotify-style message object returned by the compat endpoint (024)

    Attributes:
        id (int):
        appid (GotifyMessageAppid):
        date (datetime.datetime):
        message (str):
        title (str | Unset):
        priority (int | Unset):
        extras (GotifyMessageExtras | Unset):
    """

    id: int
    appid: GotifyMessageAppid
    date: datetime.datetime
    message: str
    title: str | Unset = UNSET
    priority: int | Unset = UNSET
    extras: GotifyMessageExtras | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        appid = self.appid.value

        date = self.date.isoformat()

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
                "id": id,
                "appid": appid,
                "date": date,
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

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.gotify_message_extras import GotifyMessageExtras

        d = dict(src_dict)
        id = d.pop("id")

        appid = GotifyMessageAppid(d.pop("appid"))

        date = datetime.datetime.fromisoformat(d.pop("date"))

        message = d.pop("message")

        title = d.pop("title", UNSET)

        priority = d.pop("priority", UNSET)

        _extras = d.pop("extras", UNSET)
        extras: GotifyMessageExtras | Unset
        if isinstance(_extras, Unset):
            extras = UNSET
        else:
            extras = GotifyMessageExtras.from_dict(_extras)

        gotify_message = cls(
            id=id,
            appid=appid,
            date=date,
            message=message,
            title=title,
            priority=priority,
            extras=extras,
        )

        gotify_message.additional_properties = d
        return gotify_message

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
