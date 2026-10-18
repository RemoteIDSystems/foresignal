from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="EventStreamFilterChronicle")


@_attrs_define
class EventStreamFilterChronicle:
    """Filter for the `chronicle` topic

    Attributes:
        chronicle_ids (list[str] | Unset):
    """

    chronicle_ids: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        chronicle_ids: list[str] | Unset = UNSET
        if not isinstance(self.chronicle_ids, Unset):
            chronicle_ids = self.chronicle_ids

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if chronicle_ids is not UNSET:
            field_dict["chronicleIds"] = chronicle_ids

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        chronicle_ids = cast(list[str], d.pop("chronicleIds", UNSET))

        event_stream_filter_chronicle = cls(
            chronicle_ids=chronicle_ids,
        )

        event_stream_filter_chronicle.additional_properties = d
        return event_stream_filter_chronicle

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
