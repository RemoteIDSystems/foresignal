from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.precursor_event_event import PrecursorEventEvent

if TYPE_CHECKING:
    from ..models.precursor_event_precursor import PrecursorEventPrecursor


T = TypeVar("T", bound="PrecursorEvent")


@_attrs_define
class PrecursorEvent:
    """SSE payload on topic `analytics` for precursor lifecycle changes

    Attributes:
        event (PrecursorEventEvent):
        precursor (PrecursorEventPrecursor): Precursor summary
        at (datetime.datetime):
    """

    event: PrecursorEventEvent
    precursor: PrecursorEventPrecursor
    at: datetime.datetime
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        event = self.event.value

        precursor = self.precursor.to_dict()

        at = self.at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "event": event,
                "precursor": precursor,
                "at": at,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.precursor_event_precursor import PrecursorEventPrecursor

        d = dict(src_dict)
        event = PrecursorEventEvent(d.pop("event"))

        precursor = PrecursorEventPrecursor.from_dict(d.pop("precursor"))

        at = datetime.datetime.fromisoformat(d.pop("at"))

        precursor_event = cls(
            event=event,
            precursor=precursor,
            at=at,
        )

        precursor_event.additional_properties = d
        return precursor_event

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
