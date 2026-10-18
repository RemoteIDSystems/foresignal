from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="EventStudyResultEventsItemCar")


@_attrs_define
class EventStudyResultEventsItemCar:
    """CAR per window

    Attributes:
        pre_event (float | None | Unset):
        event (float | None | Unset):
    """

    pre_event: float | None | Unset = UNSET
    event: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        pre_event: float | None | Unset
        if isinstance(self.pre_event, Unset):
            pre_event = UNSET
        else:
            pre_event = self.pre_event

        event: float | None | Unset
        if isinstance(self.event, Unset):
            event = UNSET
        else:
            event = self.event

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if pre_event is not UNSET:
            field_dict["preEvent"] = pre_event
        if event is not UNSET:
            field_dict["event"] = event

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_pre_event(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        pre_event = _parse_pre_event(d.pop("preEvent", UNSET))

        def _parse_event(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        event = _parse_event(d.pop("event", UNSET))

        event_study_result_events_item_car = cls(
            pre_event=pre_event,
            event=event,
        )

        event_study_result_events_item_car.additional_properties = d
        return event_study_result_events_item_car

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
