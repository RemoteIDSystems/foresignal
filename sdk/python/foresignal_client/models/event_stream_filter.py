from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.event_stream_alerts_filter import EventStreamAlertsFilter
    from ..models.event_stream_changes_filter import EventStreamChangesFilter
    from ..models.event_stream_filter_additional_property import (
        EventStreamFilterAdditionalProperty,
    )
    from ..models.event_stream_filter_chronicle import EventStreamFilterChronicle
    from ..models.event_stream_filter_external import EventStreamFilterExternal
    from ..models.event_stream_filter_notifications import (
        EventStreamFilterNotifications,
    )
    from ..models.event_stream_filter_workspace import EventStreamFilterWorkspace


T = TypeVar("T", bound="EventStreamFilter")


@_attrs_define
class EventStreamFilter:
    """Filter of a multiplexed event stream; on PATCH, omitted topic blocks are unchanged and an added topic starts live
    from now

        Example:
            {'changes': {'bbox': '-97.5,32.5,-96.5,33.2', 'live': True}}

        Attributes:
            topics (list[str] | Unset): Subscribed topics (registered by the EventTopic SPI: `changes`, `alerts`,
                `notifications`, `system`, later more)
            changes (EventStreamChangesFilter | Unset): Filter block for the `changes` topic of /v1/events/stream (same
                semantics as /v1/changes/stream)
            alerts (EventStreamAlertsFilter | Unset): Filter block for the `alerts` topic of /v1/events/stream
            notifications (EventStreamFilterNotifications | Unset): Filter block for the `notifications` topic (no filters
                defined yet)
            workspace (EventStreamFilterWorkspace | Unset): Filter block for the `workspace` topic (129)
            chronicle (EventStreamFilterChronicle | Unset): Filter for the `chronicle` topic
            external (EventStreamFilterExternal | Unset): Filter for the `external` topic (138; admin scope)
    """

    topics: list[str] | Unset = UNSET
    changes: EventStreamChangesFilter | Unset = UNSET
    alerts: EventStreamAlertsFilter | Unset = UNSET
    notifications: EventStreamFilterNotifications | Unset = UNSET
    workspace: EventStreamFilterWorkspace | Unset = UNSET
    chronicle: EventStreamFilterChronicle | Unset = UNSET
    external: EventStreamFilterExternal | Unset = UNSET
    additional_properties: dict[str, EventStreamFilterAdditionalProperty] = (
        _attrs_field(init=False, factory=dict)
    )

    def to_dict(self) -> dict[str, Any]:
        topics: list[str] | Unset = UNSET
        if not isinstance(self.topics, Unset):
            topics = self.topics

        changes: dict[str, Any] | Unset = UNSET
        if not isinstance(self.changes, Unset):
            changes = self.changes.to_dict()

        alerts: dict[str, Any] | Unset = UNSET
        if not isinstance(self.alerts, Unset):
            alerts = self.alerts.to_dict()

        notifications: dict[str, Any] | Unset = UNSET
        if not isinstance(self.notifications, Unset):
            notifications = self.notifications.to_dict()

        workspace: dict[str, Any] | Unset = UNSET
        if not isinstance(self.workspace, Unset):
            workspace = self.workspace.to_dict()

        chronicle: dict[str, Any] | Unset = UNSET
        if not isinstance(self.chronicle, Unset):
            chronicle = self.chronicle.to_dict()

        external: dict[str, Any] | Unset = UNSET
        if not isinstance(self.external, Unset):
            external = self.external.to_dict()

        field_dict: dict[str, Any] = {}
        for prop_name, prop in self.additional_properties.items():
            field_dict[prop_name] = prop.to_dict()

        field_dict.update({})
        if topics is not UNSET:
            field_dict["topics"] = topics
        if changes is not UNSET:
            field_dict["changes"] = changes
        if alerts is not UNSET:
            field_dict["alerts"] = alerts
        if notifications is not UNSET:
            field_dict["notifications"] = notifications
        if workspace is not UNSET:
            field_dict["workspace"] = workspace
        if chronicle is not UNSET:
            field_dict["chronicle"] = chronicle
        if external is not UNSET:
            field_dict["external"] = external

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.event_stream_alerts_filter import EventStreamAlertsFilter
        from ..models.event_stream_changes_filter import EventStreamChangesFilter
        from ..models.event_stream_filter_additional_property import (
            EventStreamFilterAdditionalProperty,
        )
        from ..models.event_stream_filter_chronicle import EventStreamFilterChronicle
        from ..models.event_stream_filter_external import EventStreamFilterExternal
        from ..models.event_stream_filter_notifications import (
            EventStreamFilterNotifications,
        )
        from ..models.event_stream_filter_workspace import EventStreamFilterWorkspace

        d = dict(src_dict)
        topics = cast(list[str], d.pop("topics", UNSET))

        _changes = d.pop("changes", UNSET)
        changes: EventStreamChangesFilter | Unset
        if isinstance(_changes, Unset):
            changes = UNSET
        else:
            changes = EventStreamChangesFilter.from_dict(_changes)

        _alerts = d.pop("alerts", UNSET)
        alerts: EventStreamAlertsFilter | Unset
        if isinstance(_alerts, Unset):
            alerts = UNSET
        else:
            alerts = EventStreamAlertsFilter.from_dict(_alerts)

        _notifications = d.pop("notifications", UNSET)
        notifications: EventStreamFilterNotifications | Unset
        if isinstance(_notifications, Unset):
            notifications = UNSET
        else:
            notifications = EventStreamFilterNotifications.from_dict(_notifications)

        _workspace = d.pop("workspace", UNSET)
        workspace: EventStreamFilterWorkspace | Unset
        if isinstance(_workspace, Unset):
            workspace = UNSET
        else:
            workspace = EventStreamFilterWorkspace.from_dict(_workspace)

        _chronicle = d.pop("chronicle", UNSET)
        chronicle: EventStreamFilterChronicle | Unset
        if isinstance(_chronicle, Unset):
            chronicle = UNSET
        else:
            chronicle = EventStreamFilterChronicle.from_dict(_chronicle)

        _external = d.pop("external", UNSET)
        external: EventStreamFilterExternal | Unset
        if isinstance(_external, Unset):
            external = UNSET
        else:
            external = EventStreamFilterExternal.from_dict(_external)

        event_stream_filter = cls(
            topics=topics,
            changes=changes,
            alerts=alerts,
            notifications=notifications,
            workspace=workspace,
            chronicle=chronicle,
            external=external,
        )

        additional_properties = {}
        for prop_name, prop_dict in d.items():
            additional_property = EventStreamFilterAdditionalProperty.from_dict(
                prop_dict
            )

            additional_properties[prop_name] = additional_property

        event_stream_filter.additional_properties = additional_properties
        return event_stream_filter

    @property
    def additional_keys(self) -> list[str]:
        return list(self.additional_properties.keys())

    def __getitem__(self, key: str) -> EventStreamFilterAdditionalProperty:
        return self.additional_properties[key]

    def __setitem__(self, key: str, value: EventStreamFilterAdditionalProperty) -> None:
        self.additional_properties[key] = value

    def __delitem__(self, key: str) -> None:
        del self.additional_properties[key]

    def __contains__(self, key: str) -> bool:
        return key in self.additional_properties
