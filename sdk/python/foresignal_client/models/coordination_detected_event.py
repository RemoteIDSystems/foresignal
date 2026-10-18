from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import (
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.coordination_detected_event_significant_indicators_item import (
    CoordinationDetectedEventSignificantIndicatorsItem,
)

T = TypeVar("T", bound="CoordinationDetectedEvent")


@_attrs_define
class CoordinationDetectedEvent:
    """SSE `coordination.detected` (topic `social`): a `social-coordination` finding (136)

    Attributes:
        event (Literal['coordination.detected']):
        finding_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        narrative_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        significant_indicators (list[CoordinationDetectedEventSignificantIndicatorsItem]):
        at (datetime.datetime):
    """

    event: Literal["coordination.detected"]
    finding_id: str
    narrative_id: str
    significant_indicators: list[CoordinationDetectedEventSignificantIndicatorsItem]
    at: datetime.datetime
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        event = self.event

        finding_id = self.finding_id

        narrative_id = self.narrative_id

        significant_indicators = []
        for significant_indicators_item_data in self.significant_indicators:
            significant_indicators_item = significant_indicators_item_data.value
            significant_indicators.append(significant_indicators_item)

        at = self.at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "event": event,
                "findingId": finding_id,
                "narrativeId": narrative_id,
                "significantIndicators": significant_indicators,
                "at": at,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        event = cast(Literal["coordination.detected"], d.pop("event"))
        if event != "coordination.detected":
            raise ValueError(
                f"event must match const 'coordination.detected', got '{event}'"
            )

        finding_id = d.pop("findingId")

        narrative_id = d.pop("narrativeId")

        significant_indicators = []
        _significant_indicators = d.pop("significantIndicators")
        for significant_indicators_item_data in _significant_indicators:
            significant_indicators_item = (
                CoordinationDetectedEventSignificantIndicatorsItem(
                    significant_indicators_item_data
                )
            )

            significant_indicators.append(significant_indicators_item)

        at = datetime.datetime.fromisoformat(d.pop("at"))

        coordination_detected_event = cls(
            event=event,
            finding_id=finding_id,
            narrative_id=narrative_id,
            significant_indicators=significant_indicators,
            at=at,
        )

        coordination_detected_event.additional_properties = d
        return coordination_detected_event

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
