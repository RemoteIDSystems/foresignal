from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.finding_event_event import FindingEventEvent

if TYPE_CHECKING:
    from ..models.finding_event_finding import FindingEventFinding


T = TypeVar("T", bound="FindingEvent")


@_attrs_define
class FindingEvent:
    """SSE payload on topic `analytics` for finding changes

    Attributes:
        event (FindingEventEvent):
        finding (FindingEventFinding): Finding summary
        at (datetime.datetime):
    """

    event: FindingEventEvent
    finding: FindingEventFinding
    at: datetime.datetime
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        event = self.event.value

        finding = self.finding.to_dict()

        at = self.at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "event": event,
                "finding": finding,
                "at": at,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.finding_event_finding import FindingEventFinding

        d = dict(src_dict)
        event = FindingEventEvent(d.pop("event"))

        finding = FindingEventFinding.from_dict(d.pop("finding"))

        at = datetime.datetime.fromisoformat(d.pop("at"))

        finding_event = cls(
            event=event,
            finding=finding,
            at=at,
        )

        finding_event.additional_properties = d
        return finding_event

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
