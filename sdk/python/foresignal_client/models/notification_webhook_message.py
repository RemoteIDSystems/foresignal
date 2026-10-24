from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.notification_webhook_message_event import NotificationWebhookMessageEvent
from ..models.notification_webhook_message_severity import (
    NotificationWebhookMessageSeverity,
)
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.attachment import Attachment
    from ..models.notification_link import NotificationLink
    from ..models.notification_webhook_message_data_type_0 import (
        NotificationWebhookMessageDataType0,
    )


T = TypeVar("T", bound="NotificationWebhookMessage")


@_attrs_define
class NotificationWebhookMessage:
    """The notification as the `notification` webhook delivers it (`WebhookEnvelope.message`): the same members as
    NotificationMessage, kept as its own schema so its `event` list stays `enum` (the compatibility check cannot mark a
    webhook payload's list extensible) while the event stream's is `x-extensible-enum`.

        Attributes:
            id (int): Notification id
            event (NotificationWebhookMessageEvent): Grows as the server adds events: a receiver must accept a value not
                listed here. Kept as `enum` because this is the `notification` webhook's payload, which the compatibility check
                cannot mark extensible.
            subtype (str): e.g. alert.fired, alert.acknowledged, alert.snoozed, alert.resolved, alert.digest (a quiet-hours,
                pause, backlog or grouping digest), system.source_failing, system.rule_storm, system.delivery_failed,
                system.push_provider_failed (032), briefing.run (029) Example: alert.fired.
            title (str):
            severity (NotificationWebhookMessageSeverity):
            short_title (None | str | Unset): ≤ 40 chars, for watches, mesh and push
            body_markdown (None | str | Unset): Foresignal markdown subset
            url (None | str | Unset): API link, e.g. `{publicBaseUrl}/v1/alerts/{id}`
            links (list[NotificationLink] | Unset):
            data (None | NotificationWebhookMessageDataType0 | Unset): Full event data (alert or briefing-run JSON). Alert
                data carries `context` (056): the computed alert context with `context.weather` (`WeatherContext`, its `summary`
                sentence and sections) when the alert has one
            tags (list[str] | Unset):
            attachments (list[Attachment] | Unset): Image attachments as media references (filled by 055)
            update_of (int | None | Unset): Notification this one updates (lifecycle updates)
            origin (str | Unset): Install id of the sending instance (loop guard)
            created_at (datetime.datetime | Unset):
            exercise (bool | Unset): Title prefixed [EXERCISE] and exercise footer appended Default: False.
    """

    id: int
    event: NotificationWebhookMessageEvent
    subtype: str
    title: str
    severity: NotificationWebhookMessageSeverity
    short_title: None | str | Unset = UNSET
    body_markdown: None | str | Unset = UNSET
    url: None | str | Unset = UNSET
    links: list[NotificationLink] | Unset = UNSET
    data: None | NotificationWebhookMessageDataType0 | Unset = UNSET
    tags: list[str] | Unset = UNSET
    attachments: list[Attachment] | Unset = UNSET
    update_of: int | None | Unset = UNSET
    origin: str | Unset = UNSET
    created_at: datetime.datetime | Unset = UNSET
    exercise: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.notification_webhook_message_data_type_0 import (
            NotificationWebhookMessageDataType0,
        )

        id = self.id

        event = self.event.value

        subtype = self.subtype

        title = self.title

        severity = self.severity.value

        short_title: None | str | Unset
        if isinstance(self.short_title, Unset):
            short_title = UNSET
        else:
            short_title = self.short_title

        body_markdown: None | str | Unset
        if isinstance(self.body_markdown, Unset):
            body_markdown = UNSET
        else:
            body_markdown = self.body_markdown

        url: None | str | Unset
        if isinstance(self.url, Unset):
            url = UNSET
        else:
            url = self.url

        links: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.links, Unset):
            links = []
            for links_item_data in self.links:
                links_item = links_item_data.to_dict()
                links.append(links_item)

        data: dict[str, Any] | None | Unset
        if isinstance(self.data, Unset):
            data = UNSET
        elif isinstance(self.data, NotificationWebhookMessageDataType0):
            data = self.data.to_dict()
        else:
            data = self.data

        tags: list[str] | Unset = UNSET
        if not isinstance(self.tags, Unset):
            tags = self.tags

        attachments: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.attachments, Unset):
            attachments = []
            for attachments_item_data in self.attachments:
                attachments_item = attachments_item_data.to_dict()
                attachments.append(attachments_item)

        update_of: int | None | Unset
        if isinstance(self.update_of, Unset):
            update_of = UNSET
        else:
            update_of = self.update_of

        origin = self.origin

        created_at: str | Unset = UNSET
        if not isinstance(self.created_at, Unset):
            created_at = self.created_at.isoformat()

        exercise = self.exercise

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "event": event,
                "subtype": subtype,
                "title": title,
                "severity": severity,
            }
        )
        if short_title is not UNSET:
            field_dict["shortTitle"] = short_title
        if body_markdown is not UNSET:
            field_dict["bodyMarkdown"] = body_markdown
        if url is not UNSET:
            field_dict["url"] = url
        if links is not UNSET:
            field_dict["links"] = links
        if data is not UNSET:
            field_dict["data"] = data
        if tags is not UNSET:
            field_dict["tags"] = tags
        if attachments is not UNSET:
            field_dict["attachments"] = attachments
        if update_of is not UNSET:
            field_dict["updateOf"] = update_of
        if origin is not UNSET:
            field_dict["origin"] = origin
        if created_at is not UNSET:
            field_dict["createdAt"] = created_at
        if exercise is not UNSET:
            field_dict["exercise"] = exercise

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.attachment import Attachment
        from ..models.notification_link import NotificationLink
        from ..models.notification_webhook_message_data_type_0 import (
            NotificationWebhookMessageDataType0,
        )

        d = dict(src_dict)
        id = d.pop("id")

        event = NotificationWebhookMessageEvent(d.pop("event"))

        subtype = d.pop("subtype")

        title = d.pop("title")

        severity = NotificationWebhookMessageSeverity(d.pop("severity"))

        def _parse_short_title(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        short_title = _parse_short_title(d.pop("shortTitle", UNSET))

        def _parse_body_markdown(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        body_markdown = _parse_body_markdown(d.pop("bodyMarkdown", UNSET))

        def _parse_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        url = _parse_url(d.pop("url", UNSET))

        _links = d.pop("links", UNSET)
        links: list[NotificationLink] | Unset = UNSET
        if _links is not UNSET:
            links = []
            for links_item_data in _links:
                links_item = NotificationLink.from_dict(links_item_data)

                links.append(links_item)

        def _parse_data(
            data: object,
        ) -> None | NotificationWebhookMessageDataType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                data_type_0 = NotificationWebhookMessageDataType0.from_dict(data)

                return data_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | NotificationWebhookMessageDataType0 | Unset, data)

        data = _parse_data(d.pop("data", UNSET))

        tags = cast(list[str], d.pop("tags", UNSET))

        _attachments = d.pop("attachments", UNSET)
        attachments: list[Attachment] | Unset = UNSET
        if _attachments is not UNSET:
            attachments = []
            for attachments_item_data in _attachments:
                attachments_item = Attachment.from_dict(attachments_item_data)

                attachments.append(attachments_item)

        def _parse_update_of(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        update_of = _parse_update_of(d.pop("updateOf", UNSET))

        origin = d.pop("origin", UNSET)

        _created_at = d.pop("createdAt", UNSET)
        created_at: datetime.datetime | Unset
        if isinstance(_created_at, Unset):
            created_at = UNSET
        else:
            created_at = datetime.datetime.fromisoformat(_created_at)

        exercise = d.pop("exercise", UNSET)

        notification_webhook_message = cls(
            id=id,
            event=event,
            subtype=subtype,
            title=title,
            severity=severity,
            short_title=short_title,
            body_markdown=body_markdown,
            url=url,
            links=links,
            data=data,
            tags=tags,
            attachments=attachments,
            update_of=update_of,
            origin=origin,
            created_at=created_at,
            exercise=exercise,
        )

        notification_webhook_message.additional_properties = d
        return notification_webhook_message

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
