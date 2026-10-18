from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.digest_notable_item import DigestNotableItem


T = TypeVar("T", bound="DigestStatsNotableItem")


@_attrs_define
class DigestStatsNotableItem:
    """Notable records for one kind

    Attributes:
        kind (str):
        items (list[DigestNotableItem]):
    """

    kind: str
    items: list[DigestNotableItem]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        kind = self.kind

        items = []
        for items_item_data in self.items:
            items_item = items_item_data.to_dict()
            items.append(items_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "kind": kind,
                "items": items,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.digest_notable_item import DigestNotableItem

        d = dict(src_dict)
        kind = d.pop("kind")

        items = []
        _items = d.pop("items")
        for items_item_data in _items:
            items_item = DigestNotableItem.from_dict(items_item_data)

            items.append(items_item)

        digest_stats_notable_item = cls(
            kind=kind,
            items=items,
        )

        digest_stats_notable_item.additional_properties = d
        return digest_stats_notable_item

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
