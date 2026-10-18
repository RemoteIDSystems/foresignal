from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.intel_event_stream_event_change import IntelEventStreamEventChange
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.intel_event_summary import IntelEventSummary


T = TypeVar("T", bound="IntelEventStreamEvent")


@_attrs_define
class IntelEventStreamEvent:
    """SSE payload on topic `intel` (`intel.event.created|updated|resolved|merged`)

    Attributes:
        event_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        change (IntelEventStreamEventChange):
        version (int):
        summary (IntelEventSummary): Compact intelligence event for lists and SSE
        merged_into (None | str | Unset):
    """

    event_id: str
    change: IntelEventStreamEventChange
    version: int
    summary: IntelEventSummary
    merged_into: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        event_id = self.event_id

        change = self.change.value

        version = self.version

        summary = self.summary.to_dict()

        merged_into: None | str | Unset
        if isinstance(self.merged_into, Unset):
            merged_into = UNSET
        else:
            merged_into = self.merged_into

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "eventId": event_id,
                "change": change,
                "version": version,
                "summary": summary,
            }
        )
        if merged_into is not UNSET:
            field_dict["mergedInto"] = merged_into

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.intel_event_summary import IntelEventSummary

        d = dict(src_dict)
        event_id = d.pop("eventId")

        change = IntelEventStreamEventChange(d.pop("change"))

        version = d.pop("version")

        summary = IntelEventSummary.from_dict(d.pop("summary"))

        def _parse_merged_into(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        merged_into = _parse_merged_into(d.pop("mergedInto", UNSET))

        intel_event_stream_event = cls(
            event_id=event_id,
            change=change,
            version=version,
            summary=summary,
            merged_into=merged_into,
        )

        intel_event_stream_event.additional_properties = d
        return intel_event_stream_event

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
