from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="EventStudyResultWindows")


@_attrs_define
class EventStudyResultWindows:
    """Windows in buckets of the series' frequency

    Attributes:
        estimation (list[int]):
        pre_event (list[int]):
        event (list[int]):
    """

    estimation: list[int]
    pre_event: list[int]
    event: list[int]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        estimation = self.estimation

        pre_event = self.pre_event

        event = self.event

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "estimation": estimation,
                "preEvent": pre_event,
                "event": event,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        estimation = cast(list[int], d.pop("estimation"))

        pre_event = cast(list[int], d.pop("preEvent"))

        event = cast(list[int], d.pop("event"))

        event_study_result_windows = cls(
            estimation=estimation,
            pre_event=pre_event,
            event=event,
        )

        event_study_result_windows.additional_properties = d
        return event_study_result_windows

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
