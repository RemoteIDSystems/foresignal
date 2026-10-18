from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="ProductTemporalRetentionLadderItem")


@_attrs_define
class ProductTemporalRetentionLadderItem:
    """Thinning rung: frames older than `olderThan` are kept every `keepEvery`

    Attributes:
        older_than (str): ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 6h.
        keep_every (str): ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 10m.
    """

    older_than: str
    keep_every: str
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        older_than = self.older_than

        keep_every = self.keep_every

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "olderThan": older_than,
                "keepEvery": keep_every,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        older_than = d.pop("olderThan")

        keep_every = d.pop("keepEvery")

        product_temporal_retention_ladder_item = cls(
            older_than=older_than,
            keep_every=keep_every,
        )

        product_temporal_retention_ladder_item.additional_properties = d
        return product_temporal_retention_ladder_item

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
