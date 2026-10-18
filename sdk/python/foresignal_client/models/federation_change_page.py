from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.federation_item import FederationItem


T = TypeVar("T", bound="FederationChangePage")


@_attrs_define
class FederationChangePage:
    """Federation change feed page (113)

    Attributes:
        items (list[FederationItem]):
        last_seq (int): Pass as `after` next time
        has_more (bool):
        watermark_seq (int | Unset):
    """

    items: list[FederationItem]
    last_seq: int
    has_more: bool
    watermark_seq: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        items = []
        for items_item_data in self.items:
            items_item = items_item_data.to_dict()
            items.append(items_item)

        last_seq = self.last_seq

        has_more = self.has_more

        watermark_seq = self.watermark_seq

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "items": items,
                "lastSeq": last_seq,
                "hasMore": has_more,
            }
        )
        if watermark_seq is not UNSET:
            field_dict["watermarkSeq"] = watermark_seq

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.federation_item import FederationItem

        d = dict(src_dict)
        items = []
        _items = d.pop("items")
        for items_item_data in _items:
            items_item = FederationItem.from_dict(items_item_data)

            items.append(items_item)

        last_seq = d.pop("lastSeq")

        has_more = d.pop("hasMore")

        watermark_seq = d.pop("watermarkSeq", UNSET)

        federation_change_page = cls(
            items=items,
            last_seq=last_seq,
            has_more=has_more,
            watermark_seq=watermark_seq,
        )

        federation_change_page.additional_properties = d
        return federation_change_page

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
