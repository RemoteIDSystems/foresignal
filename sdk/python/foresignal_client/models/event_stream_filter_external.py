from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="EventStreamFilterExternal")


@_attrs_define
class EventStreamFilterExternal:
    """Filter for the `external` topic (138; admin scope)

    Attributes:
        source_ids (list[str] | Unset): Only these providers
    """

    source_ids: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source_ids: list[str] | Unset = UNSET
        if not isinstance(self.source_ids, Unset):
            source_ids = self.source_ids

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if source_ids is not UNSET:
            field_dict["sourceIds"] = source_ids

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        source_ids = cast(list[str], d.pop("sourceIds", UNSET))

        event_stream_filter_external = cls(
            source_ids=source_ids,
        )

        event_stream_filter_external.additional_properties = d
        return event_stream_filter_external

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
