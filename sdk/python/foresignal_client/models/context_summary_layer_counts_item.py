from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ContextSummaryLayerCountsItem")


@_attrs_define
class ContextSummaryLayerCountsItem:
    """Aggregated count for a layer or kind in the viewport

    Attributes:
        layer (str):
        count (int):
        live_count (int | None | Unset):
    """

    layer: str
    count: int
    live_count: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        layer = self.layer

        count = self.count

        live_count: int | None | Unset
        if isinstance(self.live_count, Unset):
            live_count = UNSET
        else:
            live_count = self.live_count

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "layer": layer,
                "count": count,
            }
        )
        if live_count is not UNSET:
            field_dict["liveCount"] = live_count

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        layer = d.pop("layer")

        count = d.pop("count")

        def _parse_live_count(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        live_count = _parse_live_count(d.pop("liveCount", UNSET))

        context_summary_layer_counts_item = cls(
            layer=layer,
            count=count,
            live_count=live_count,
        )

        context_summary_layer_counts_item.additional_properties = d
        return context_summary_layer_counts_item

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
