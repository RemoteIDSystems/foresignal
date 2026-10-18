from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ExposureWatchlistEntitiesCountsItem")


@_attrs_define
class ExposureWatchlistEntitiesCountsItem:
    """Count per watchlist

    Attributes:
        watchlist_id (str):
        count (int):
        name (str | Unset):
    """

    watchlist_id: str
    count: int
    name: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        watchlist_id = self.watchlist_id

        count = self.count

        name = self.name

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "watchlistId": watchlist_id,
                "count": count,
            }
        )
        if name is not UNSET:
            field_dict["name"] = name

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        watchlist_id = d.pop("watchlistId")

        count = d.pop("count")

        name = d.pop("name", UNSET)

        exposure_watchlist_entities_counts_item = cls(
            watchlist_id=watchlist_id,
            count=count,
            name=name,
        )

        exposure_watchlist_entities_counts_item.additional_properties = d
        return exposure_watchlist_entities_counts_item

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
