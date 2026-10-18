from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.event_study_window_stats import EventStudyWindowStats


T = TypeVar("T", bound="EventStudyResultCaar")


@_attrs_define
class EventStudyResultCaar:
    """CAAR and tests per window

    Attributes:
        pre_event (EventStudyWindowStats): Cross-event statistics for one window
        event (EventStudyWindowStats): Cross-event statistics for one window
    """

    pre_event: EventStudyWindowStats
    event: EventStudyWindowStats
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        pre_event = self.pre_event.to_dict()

        event = self.event.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "preEvent": pre_event,
                "event": event,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.event_study_window_stats import EventStudyWindowStats

        d = dict(src_dict)
        pre_event = EventStudyWindowStats.from_dict(d.pop("preEvent"))

        event = EventStudyWindowStats.from_dict(d.pop("event"))

        event_study_result_caar = cls(
            pre_event=pre_event,
            event=event,
        )

        event_study_result_caar.additional_properties = d
        return event_study_result_caar

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
