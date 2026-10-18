from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.watchlist import Watchlist
    from ..models.watchlist_item import WatchlistItem


T = TypeVar("T", bound="SettingsBundleWatchlistsItem")


@_attrs_define
class SettingsBundleWatchlistsItem:
    """Watchlist with items

    Attributes:
        watchlist (Watchlist | Unset): A watchlist of identifiers (036); up to 100,000 items
        items (list[WatchlistItem] | Unset):
    """

    watchlist: Watchlist | Unset = UNSET
    items: list[WatchlistItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        watchlist: dict[str, Any] | Unset = UNSET
        if not isinstance(self.watchlist, Unset):
            watchlist = self.watchlist.to_dict()

        items: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.items, Unset):
            items = []
            for items_item_data in self.items:
                items_item = items_item_data.to_dict()
                items.append(items_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if watchlist is not UNSET:
            field_dict["watchlist"] = watchlist
        if items is not UNSET:
            field_dict["items"] = items

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.watchlist import Watchlist
        from ..models.watchlist_item import WatchlistItem

        d = dict(src_dict)
        _watchlist = d.pop("watchlist", UNSET)
        watchlist: Watchlist | Unset
        if isinstance(_watchlist, Unset):
            watchlist = UNSET
        else:
            watchlist = Watchlist.from_dict(_watchlist)

        _items = d.pop("items", UNSET)
        items: list[WatchlistItem] | Unset = UNSET
        if _items is not UNSET:
            items = []
            for items_item_data in _items:
                items_item = WatchlistItem.from_dict(items_item_data)

                items.append(items_item)

        settings_bundle_watchlists_item = cls(
            watchlist=watchlist,
            items=items,
        )

        settings_bundle_watchlists_item.additional_properties = d
        return settings_bundle_watchlists_item

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
