from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.fetch_log_entry import FetchLogEntry


T = TypeVar("T", bound="FetchPage")


@_attrs_define
class FetchPage:
    """Keyset page of fetch-log entries, newest first

    Attributes:
        items (list[FetchLogEntry]):
        next_after (int | None | Unset): Pass as `after` for the next page; null on the last page
    """

    items: list[FetchLogEntry]
    next_after: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        items = []
        for items_item_data in self.items:
            items_item = items_item_data.to_dict()
            items.append(items_item)

        next_after: int | None | Unset
        if isinstance(self.next_after, Unset):
            next_after = UNSET
        else:
            next_after = self.next_after

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "items": items,
            }
        )
        if next_after is not UNSET:
            field_dict["nextAfter"] = next_after

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.fetch_log_entry import FetchLogEntry

        d = dict(src_dict)
        items = []
        _items = d.pop("items")
        for items_item_data in _items:
            items_item = FetchLogEntry.from_dict(items_item_data)

            items.append(items_item)

        def _parse_next_after(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        next_after = _parse_next_after(d.pop("nextAfter", UNSET))

        fetch_page = cls(
            items=items,
            next_after=next_after,
        )

        fetch_page.additional_properties = d
        return fetch_page

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
