from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="EgressHealthSourcesItem")


@_attrs_define
class EgressHealthSourcesItem:
    """
    Attributes:
        source_id (str):
        backoff_until (datetime.datetime):
        consecutive_throttles (int):
    """

    source_id: str
    backoff_until: datetime.datetime
    consecutive_throttles: int
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source_id = self.source_id

        backoff_until = self.backoff_until.isoformat()

        consecutive_throttles = self.consecutive_throttles

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sourceId": source_id,
                "backoffUntil": backoff_until,
                "consecutiveThrottles": consecutive_throttles,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        source_id = d.pop("sourceId")

        backoff_until = datetime.datetime.fromisoformat(d.pop("backoffUntil"))

        consecutive_throttles = d.pop("consecutiveThrottles")

        egress_health_sources_item = cls(
            source_id=source_id,
            backoff_until=backoff_until,
            consecutive_throttles=consecutive_throttles,
        )

        egress_health_sources_item.additional_properties = d
        return egress_health_sources_item

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
