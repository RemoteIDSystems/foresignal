from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SocialPlatformPrice")


@_attrs_define
class SocialPlatformPrice:
    """One operator-editable price

    Attributes:
        item (str): Billable item, e.g. `counts-request`, `post-read`, `search-request` Example: post-read.
        usd (float): US dollars
        per (int | Unset): Units the price covers Default: 1.
    """

    item: str
    usd: float
    per: int | Unset = 1
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        item = self.item

        usd = self.usd

        per = self.per

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "item": item,
                "usd": usd,
            }
        )
        if per is not UNSET:
            field_dict["per"] = per

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        item = d.pop("item")

        usd = d.pop("usd")

        per = d.pop("per", UNSET)

        social_platform_price = cls(
            item=item,
            usd=usd,
            per=per,
        )

        social_platform_price.additional_properties = d
        return social_platform_price

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
