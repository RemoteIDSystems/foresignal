from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="EventStudyResultEventsItemAbnormalItem")


@_attrs_define
class EventStudyResultEventsItemAbnormalItem:
    """Abnormal value at an offset

    Attributes:
        offset (int):
        value (float | None | Unset):
        cumulative (float | None | Unset):
    """

    offset: int
    value: float | None | Unset = UNSET
    cumulative: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        offset = self.offset

        value: float | None | Unset
        if isinstance(self.value, Unset):
            value = UNSET
        else:
            value = self.value

        cumulative: float | None | Unset
        if isinstance(self.cumulative, Unset):
            cumulative = UNSET
        else:
            cumulative = self.cumulative

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "offset": offset,
            }
        )
        if value is not UNSET:
            field_dict["value"] = value
        if cumulative is not UNSET:
            field_dict["cumulative"] = cumulative

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        offset = d.pop("offset")

        def _parse_value(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        value = _parse_value(d.pop("value", UNSET))

        def _parse_cumulative(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        cumulative = _parse_cumulative(d.pop("cumulative", UNSET))

        event_study_result_events_item_abnormal_item = cls(
            offset=offset,
            value=value,
            cumulative=cumulative,
        )

        event_study_result_events_item_abnormal_item.additional_properties = d
        return event_study_result_events_item_abnormal_item

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
