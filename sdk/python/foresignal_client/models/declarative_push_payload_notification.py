from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.push_payload import PushPayload


T = TypeVar("T", bound="DeclarativePushPayloadNotification")


@_attrs_define
class DeclarativePushPayloadNotification:
    """Declarative notification

    Attributes:
        title (str):
        navigate (str): links.web ?? links.api (033: the confirm page for actions)
        body (str | Unset):
        lang (str | Unset):
        tag (str | Unset):
        silent (bool | Unset):
        data (PushPayload | Unset): Plaintext Foresignal push message before RFC 8291 encryption (031), ≤ 3,993 bytes;
            used when `payloadFormat=foresignal` (the `declarative` format wraps it in DeclarativePushPayload). A new
            subscription first receives `{v, event: "verify", subscriptionId, code}`. Shrunk in order (body, links.api,
            tags, title) until it fits; last resort `{v, event, id, severity, shortTitle}`.
    """

    title: str
    navigate: str
    body: str | Unset = UNSET
    lang: str | Unset = UNSET
    tag: str | Unset = UNSET
    silent: bool | Unset = UNSET
    data: PushPayload | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        title = self.title

        navigate = self.navigate

        body = self.body

        lang = self.lang

        tag = self.tag

        silent = self.silent

        data: dict[str, Any] | Unset = UNSET
        if not isinstance(self.data, Unset):
            data = self.data.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "title": title,
                "navigate": navigate,
            }
        )
        if body is not UNSET:
            field_dict["body"] = body
        if lang is not UNSET:
            field_dict["lang"] = lang
        if tag is not UNSET:
            field_dict["tag"] = tag
        if silent is not UNSET:
            field_dict["silent"] = silent
        if data is not UNSET:
            field_dict["data"] = data

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.push_payload import PushPayload

        d = dict(src_dict)
        title = d.pop("title")

        navigate = d.pop("navigate")

        body = d.pop("body", UNSET)

        lang = d.pop("lang", UNSET)

        tag = d.pop("tag", UNSET)

        silent = d.pop("silent", UNSET)

        _data = d.pop("data", UNSET)
        data: PushPayload | Unset
        if isinstance(_data, Unset):
            data = UNSET
        else:
            data = PushPayload.from_dict(_data)

        declarative_push_payload_notification = cls(
            title=title,
            navigate=navigate,
            body=body,
            lang=lang,
            tag=tag,
            silent=silent,
            data=data,
        )

        declarative_push_payload_notification.additional_properties = d
        return declarative_push_payload_notification

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
