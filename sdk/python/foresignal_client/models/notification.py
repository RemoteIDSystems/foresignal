from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.notification_alert_state_type_1 import NotificationAlertStateType1
from ..models.notification_alert_state_type_2_type_1 import (
    NotificationAlertStateType2Type1,
)
from ..models.notification_alert_state_type_3_type_1 import (
    NotificationAlertStateType3Type1,
)
from ..models.notification_event import NotificationEvent
from ..models.notification_severity import NotificationSeverity
from ..types import UNSET, Unset

T = TypeVar("T", bound="Notification")


@_attrs_define
class Notification:
    """An inbox item: a persisted notification with per-device read state (033)

    Attributes:
        id (int):
        event (NotificationEvent):
        severity (NotificationSeverity):
        title (str):
        created_at (datetime.datetime):
        read (bool): Read on this device; reading never acknowledges an alert
        subtype (None | str | Unset):  Example: alert.fired.
        body (str | Unset): Markdown
        alert_id (int | None | Unset):
        briefing_run_id (None | Unset | UUID):
        rule_id (None | Unset | UUID):
        alert_state (None | NotificationAlertStateType1 | NotificationAlertStateType2Type1 |
            NotificationAlertStateType3Type1 | Unset): Live alert state
    """

    id: int
    event: NotificationEvent
    severity: NotificationSeverity
    title: str
    created_at: datetime.datetime
    read: bool
    subtype: None | str | Unset = UNSET
    body: str | Unset = UNSET
    alert_id: int | None | Unset = UNSET
    briefing_run_id: None | Unset | UUID = UNSET
    rule_id: None | Unset | UUID = UNSET
    alert_state: (
        None
        | NotificationAlertStateType1
        | NotificationAlertStateType2Type1
        | NotificationAlertStateType3Type1
        | Unset
    ) = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        event = self.event.value

        severity = self.severity.value

        title = self.title

        created_at = self.created_at.isoformat()

        read = self.read

        subtype: None | str | Unset
        if isinstance(self.subtype, Unset):
            subtype = UNSET
        else:
            subtype = self.subtype

        body = self.body

        alert_id: int | None | Unset
        if isinstance(self.alert_id, Unset):
            alert_id = UNSET
        else:
            alert_id = self.alert_id

        briefing_run_id: None | str | Unset
        if isinstance(self.briefing_run_id, Unset):
            briefing_run_id = UNSET
        elif isinstance(self.briefing_run_id, UUID):
            briefing_run_id = str(self.briefing_run_id)
        else:
            briefing_run_id = self.briefing_run_id

        rule_id: None | str | Unset
        if isinstance(self.rule_id, Unset):
            rule_id = UNSET
        elif isinstance(self.rule_id, UUID):
            rule_id = str(self.rule_id)
        else:
            rule_id = self.rule_id

        alert_state: None | str | Unset
        if isinstance(self.alert_state, Unset):
            alert_state = UNSET
        elif (
            isinstance(self.alert_state, NotificationAlertStateType1)
            or isinstance(self.alert_state, NotificationAlertStateType2Type1)
            or isinstance(self.alert_state, NotificationAlertStateType3Type1)
        ):
            alert_state = self.alert_state.value
        else:
            alert_state = self.alert_state

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "event": event,
                "severity": severity,
                "title": title,
                "createdAt": created_at,
                "read": read,
            }
        )
        if subtype is not UNSET:
            field_dict["subtype"] = subtype
        if body is not UNSET:
            field_dict["body"] = body
        if alert_id is not UNSET:
            field_dict["alertId"] = alert_id
        if briefing_run_id is not UNSET:
            field_dict["briefingRunId"] = briefing_run_id
        if rule_id is not UNSET:
            field_dict["ruleId"] = rule_id
        if alert_state is not UNSET:
            field_dict["alertState"] = alert_state

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        event = NotificationEvent(d.pop("event"))

        severity = NotificationSeverity(d.pop("severity"))

        title = d.pop("title")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        read = d.pop("read")

        def _parse_subtype(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        subtype = _parse_subtype(d.pop("subtype", UNSET))

        body = d.pop("body", UNSET)

        def _parse_alert_id(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        alert_id = _parse_alert_id(d.pop("alertId", UNSET))

        def _parse_briefing_run_id(data: object) -> None | Unset | UUID:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                briefing_run_id_type_0 = UUID(data)

                return briefing_run_id_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Unset | UUID, data)

        briefing_run_id = _parse_briefing_run_id(d.pop("briefingRunId", UNSET))

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

        def _parse_alert_state(
            data: object,
        ) -> (
            None
            | NotificationAlertStateType1
            | NotificationAlertStateType2Type1
            | NotificationAlertStateType3Type1
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                alert_state_type_1 = NotificationAlertStateType1(data)

                return alert_state_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                alert_state_type_2_type_1 = NotificationAlertStateType2Type1(data)

                return alert_state_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                alert_state_type_3_type_1 = NotificationAlertStateType3Type1(data)

                return alert_state_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | NotificationAlertStateType1
                | NotificationAlertStateType2Type1
                | NotificationAlertStateType3Type1
                | Unset,
                data,
            )

        alert_state = _parse_alert_state(d.pop("alertState", UNSET))

        notification = cls(
            id=id,
            event=event,
            severity=severity,
            title=title,
            created_at=created_at,
            read=read,
            subtype=subtype,
            body=body,
            alert_id=alert_id,
            briefing_run_id=briefing_run_id,
            rule_id=rule_id,
            alert_state=alert_state,
        )

        notification.additional_properties = d
        return notification

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
