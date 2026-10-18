from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.chronicle_thread_item import ChronicleThreadItem


T = TypeVar("T", bound="ChronicleThreadPage")


@_attrs_define
class ChronicleThreadPage:
    """One page of a chronicle thread

    Attributes:
        items (list[ChronicleThreadItem]):
        next_cursor (None | str): Opaque cursor for the next page; null at the end
        unread_count (int | Unset):
        read_up_to (int | None | Unset): Caller's read cursor
    """

    items: list[ChronicleThreadItem]
    next_cursor: None | str
    unread_count: int | Unset = UNSET
    read_up_to: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        items = []
        for items_item_data in self.items:
            items_item = items_item_data.to_dict()
            items.append(items_item)

        next_cursor: None | str
        next_cursor = self.next_cursor

        unread_count = self.unread_count

        read_up_to: int | None | Unset
        if isinstance(self.read_up_to, Unset):
            read_up_to = UNSET
        else:
            read_up_to = self.read_up_to

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "items": items,
                "nextCursor": next_cursor,
            }
        )
        if unread_count is not UNSET:
            field_dict["unreadCount"] = unread_count
        if read_up_to is not UNSET:
            field_dict["readUpTo"] = read_up_to

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.chronicle_thread_item import ChronicleThreadItem

        d = dict(src_dict)
        items = []
        _items = d.pop("items")
        for items_item_data in _items:
            items_item = ChronicleThreadItem.from_dict(items_item_data)

            items.append(items_item)

        def _parse_next_cursor(data: object) -> None | str:
            if data is None:
                return data
            return cast(None | str, data)

        next_cursor = _parse_next_cursor(d.pop("nextCursor"))

        unread_count = d.pop("unreadCount", UNSET)

        def _parse_read_up_to(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        read_up_to = _parse_read_up_to(d.pop("readUpTo", UNSET))

        chronicle_thread_page = cls(
            items=items,
            next_cursor=next_cursor,
            unread_count=unread_count,
            read_up_to=read_up_to,
        )

        chronicle_thread_page.additional_properties = d
        return chronicle_thread_page

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
