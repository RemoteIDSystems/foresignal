from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.narrative_event_changed_item import NarrativeEventChangedItem
from ..models.narrative_event_event import NarrativeEventEvent
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.narrative_summary import NarrativeSummary


T = TypeVar("T", bound="NarrativeEvent")


@_attrs_define
class NarrativeEvent:
    """SSE `narrative.emerged` | `narrative.updated` (topic `social`); updates fire when state or counts cross a band (136)

    Attributes:
        event (NarrativeEventEvent):
        narrative (NarrativeSummary): A narrative: posts that say the same thing, a 116 canonical event (family `social-
            narrative`) whose evidence groups count independent origins (136)
        at (datetime.datetime):
        changed (list[NarrativeEventChangedItem] | Unset):
    """

    event: NarrativeEventEvent
    narrative: NarrativeSummary
    at: datetime.datetime
    changed: list[NarrativeEventChangedItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        event = self.event.value

        narrative = self.narrative.to_dict()

        at = self.at.isoformat()

        changed: list[str] | Unset = UNSET
        if not isinstance(self.changed, Unset):
            changed = []
            for changed_item_data in self.changed:
                changed_item = changed_item_data.value
                changed.append(changed_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "event": event,
                "narrative": narrative,
                "at": at,
            }
        )
        if changed is not UNSET:
            field_dict["changed"] = changed

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.narrative_summary import NarrativeSummary

        d = dict(src_dict)
        event = NarrativeEventEvent(d.pop("event"))

        narrative = NarrativeSummary.from_dict(d.pop("narrative"))

        at = datetime.datetime.fromisoformat(d.pop("at"))

        _changed = d.pop("changed", UNSET)
        changed: list[NarrativeEventChangedItem] | Unset = UNSET
        if _changed is not UNSET:
            changed = []
            for changed_item_data in _changed:
                changed_item = NarrativeEventChangedItem(changed_item_data)

                changed.append(changed_item)

        narrative_event = cls(
            event=event,
            narrative=narrative,
            at=at,
            changed=changed,
        )

        narrative_event.additional_properties = d
        return narrative_event

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
