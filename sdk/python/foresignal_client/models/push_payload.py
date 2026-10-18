from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.push_payload_event import PushPayloadEvent
from ..models.push_payload_severity import PushPayloadSeverity
from ..models.push_payload_v import PushPayloadV
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.push_payload_actions_item import PushPayloadActionsItem
    from ..models.push_payload_links import PushPayloadLinks


T = TypeVar("T", bound="PushPayload")


@_attrs_define
class PushPayload:
    """Plaintext Foresignal push message before RFC 8291 encryption (031), ≤ 3,993 bytes; used when
    `payloadFormat=foresignal` (the `declarative` format wraps it in DeclarativePushPayload). A new subscription first
    receives `{v, event: "verify", subscriptionId, code}`. Shrunk in order (body, links.api, tags, title) until it fits;
    last resort `{v, event, id, severity, shortTitle}`.

        Attributes:
            v (PushPayloadV):
            event (PushPayloadEvent):
            id (int | Unset): Persisted notification id (018); alert id if absent
            alert_id (int | None | Unset):
            rule_id (None | Unset | UUID):
            severity (PushPayloadSeverity | Unset):
            title (str | Unset):
            short_title (str | Unset):
            body (str | Unset): Plain text; briefings send a 200-char excerpt; system events title and code only
            url (str | Unset): Alias of links.api
            links (PushPayloadLinks | Unset): Deep links (023 clientLinks; absent ones omitted)
            tags (list[str] | Unset):
            lang (str | Unset):
            tag (str | Unset): foresignal-alert-<alertId> or foresignal-<event>-<id>; repeats replace each other in the OS
            silent (bool | Unset):
            ts (int | Unset):
            actions (list[PushPayloadActionsItem] | Unset):
            confirm_url (str | Unset): Confirm page when the browser has no action buttons (033)
            subscription_id (UUID | Unset): verify only
            code (str | Unset): verify only: 16 random bytes, base64url
            exercise (bool | Unset): Exercise; urgency forced to normal Default: False.
    """

    v: PushPayloadV
    event: PushPayloadEvent
    id: int | Unset = UNSET
    alert_id: int | None | Unset = UNSET
    rule_id: None | Unset | UUID = UNSET
    severity: PushPayloadSeverity | Unset = UNSET
    title: str | Unset = UNSET
    short_title: str | Unset = UNSET
    body: str | Unset = UNSET
    url: str | Unset = UNSET
    links: PushPayloadLinks | Unset = UNSET
    tags: list[str] | Unset = UNSET
    lang: str | Unset = UNSET
    tag: str | Unset = UNSET
    silent: bool | Unset = UNSET
    ts: int | Unset = UNSET
    actions: list[PushPayloadActionsItem] | Unset = UNSET
    confirm_url: str | Unset = UNSET
    subscription_id: UUID | Unset = UNSET
    code: str | Unset = UNSET
    exercise: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        v = self.v.value

        event = self.event.value

        id = self.id

        alert_id: int | None | Unset
        if isinstance(self.alert_id, Unset):
            alert_id = UNSET
        else:
            alert_id = self.alert_id

        rule_id: None | str | Unset
        if isinstance(self.rule_id, Unset):
            rule_id = UNSET
        elif isinstance(self.rule_id, UUID):
            rule_id = str(self.rule_id)
        else:
            rule_id = self.rule_id

        severity: str | Unset = UNSET
        if not isinstance(self.severity, Unset):
            severity = self.severity.value

        title = self.title

        short_title = self.short_title

        body = self.body

        url = self.url

        links: dict[str, Any] | Unset = UNSET
        if not isinstance(self.links, Unset):
            links = self.links.to_dict()

        tags: list[str] | Unset = UNSET
        if not isinstance(self.tags, Unset):
            tags = self.tags

        lang = self.lang

        tag = self.tag

        silent = self.silent

        ts = self.ts

        actions: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.actions, Unset):
            actions = []
            for actions_item_data in self.actions:
                actions_item = actions_item_data.to_dict()
                actions.append(actions_item)

        confirm_url = self.confirm_url

        subscription_id: str | Unset = UNSET
        if not isinstance(self.subscription_id, Unset):
            subscription_id = str(self.subscription_id)

        code = self.code

        exercise = self.exercise

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "v": v,
                "event": event,
            }
        )
        if id is not UNSET:
            field_dict["id"] = id
        if alert_id is not UNSET:
            field_dict["alertId"] = alert_id
        if rule_id is not UNSET:
            field_dict["ruleId"] = rule_id
        if severity is not UNSET:
            field_dict["severity"] = severity
        if title is not UNSET:
            field_dict["title"] = title
        if short_title is not UNSET:
            field_dict["shortTitle"] = short_title
        if body is not UNSET:
            field_dict["body"] = body
        if url is not UNSET:
            field_dict["url"] = url
        if links is not UNSET:
            field_dict["links"] = links
        if tags is not UNSET:
            field_dict["tags"] = tags
        if lang is not UNSET:
            field_dict["lang"] = lang
        if tag is not UNSET:
            field_dict["tag"] = tag
        if silent is not UNSET:
            field_dict["silent"] = silent
        if ts is not UNSET:
            field_dict["ts"] = ts
        if actions is not UNSET:
            field_dict["actions"] = actions
        if confirm_url is not UNSET:
            field_dict["confirmUrl"] = confirm_url
        if subscription_id is not UNSET:
            field_dict["subscriptionId"] = subscription_id
        if code is not UNSET:
            field_dict["code"] = code
        if exercise is not UNSET:
            field_dict["exercise"] = exercise

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.push_payload_actions_item import PushPayloadActionsItem
        from ..models.push_payload_links import PushPayloadLinks

        d = dict(src_dict)
        v = PushPayloadV(d.pop("v"))

        event = PushPayloadEvent(d.pop("event"))

        id = d.pop("id", UNSET)

        def _parse_alert_id(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        alert_id = _parse_alert_id(d.pop("alertId", UNSET))

        def _parse_rule_id(data: object) -> None | Unset | UUID:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                rule_id_type_0 = UUID(data)

                return rule_id_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Unset | UUID, data)

        rule_id = _parse_rule_id(d.pop("ruleId", UNSET))

        _severity = d.pop("severity", UNSET)
        severity: PushPayloadSeverity | Unset
        if isinstance(_severity, Unset):
            severity = UNSET
        else:
            severity = PushPayloadSeverity(_severity)

        title = d.pop("title", UNSET)

        short_title = d.pop("shortTitle", UNSET)

        body = d.pop("body", UNSET)

        url = d.pop("url", UNSET)

        _links = d.pop("links", UNSET)
        links: PushPayloadLinks | Unset
        if isinstance(_links, Unset):
            links = UNSET
        else:
            links = PushPayloadLinks.from_dict(_links)

        tags = cast(list[str], d.pop("tags", UNSET))

        lang = d.pop("lang", UNSET)

        tag = d.pop("tag", UNSET)

        silent = d.pop("silent", UNSET)

        ts = d.pop("ts", UNSET)

        _actions = d.pop("actions", UNSET)
        actions: list[PushPayloadActionsItem] | Unset = UNSET
        if _actions is not UNSET:
            actions = []
            for actions_item_data in _actions:
                actions_item = PushPayloadActionsItem.from_dict(actions_item_data)

                actions.append(actions_item)

        confirm_url = d.pop("confirmUrl", UNSET)

        _subscription_id = d.pop("subscriptionId", UNSET)
        subscription_id: UUID | Unset
        if isinstance(_subscription_id, Unset):
            subscription_id = UNSET
        else:
            subscription_id = UUID(_subscription_id)

        code = d.pop("code", UNSET)

        exercise = d.pop("exercise", UNSET)

        push_payload = cls(
            v=v,
            event=event,
            id=id,
            alert_id=alert_id,
            rule_id=rule_id,
            severity=severity,
            title=title,
            short_title=short_title,
            body=body,
            url=url,
            links=links,
            tags=tags,
            lang=lang,
            tag=tag,
            silent=silent,
            ts=ts,
            actions=actions,
            confirm_url=confirm_url,
            subscription_id=subscription_id,
            code=code,
            exercise=exercise,
        )

        push_payload.additional_properties = d
        return push_payload

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
