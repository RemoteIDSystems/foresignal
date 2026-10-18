from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.declarative_push_payload_web_push import DeclarativePushPayloadWebPush

if TYPE_CHECKING:
    from ..models.declarative_push_payload_notification import (
        DeclarativePushPayloadNotification,
    )


T = TypeVar("T", bound="DeclarativePushPayload")


@_attrs_define
class DeclarativePushPayload:
    """WebKit Declarative Web Push payload (Safari 18.4+, `payloadFormat=declarative`) (031). Members checked against
    WebKit's 'Meet Declarative Web Push' (2025): required `web_push: 8030`, `notification.title` and
    `notification.navigate`; optional `body`, `lang`, `dir`, `silent`, `tag`, `data` and other NotificationOptions
    members, and `notification.app_badge`. Foresignal never sends `app_badge` (a crash was reported in Safari 18.5) or
    `dir`. The verification push of a declarative subscription is a declarative notification whose body carries the code
    and whose `data` is the verify payload.

        Attributes:
            web_push (DeclarativePushPayloadWebPush):
            notification (DeclarativePushPayloadNotification): Declarative notification
    """

    web_push: DeclarativePushPayloadWebPush
    notification: DeclarativePushPayloadNotification
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        web_push = self.web_push.value

        notification = self.notification.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "web_push": web_push,
                "notification": notification,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.declarative_push_payload_notification import (
            DeclarativePushPayloadNotification,
        )

        d = dict(src_dict)
        web_push = DeclarativePushPayloadWebPush(d.pop("web_push"))

        notification = DeclarativePushPayloadNotification.from_dict(
            d.pop("notification")
        )

        declarative_push_payload = cls(
            web_push=web_push,
            notification=notification,
        )

        declarative_push_payload.additional_properties = d
        return declarative_push_payload

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
