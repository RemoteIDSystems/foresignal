from __future__ import annotations

from collections.abc import Mapping
from typing import (
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="ChronicleLayerSelectorType2")


@_attrs_define
class ChronicleLayerSelectorType2:
    """A 036 watchlist

    Attributes:
        type_ (Literal['watchlist']):
        watchlist_id (str):
    """

    type_: Literal["watchlist"]
    watchlist_id: str
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_

        watchlist_id = self.watchlist_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
                "watchlistId": watchlist_id,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        type_ = cast(Literal["watchlist"], d.pop("type"))
        if type_ != "watchlist":
            raise ValueError(f"type must match const 'watchlist', got '{type_}'")

        watchlist_id = d.pop("watchlistId")

        chronicle_layer_selector_type_2 = cls(
            type_=type_,
            watchlist_id=watchlist_id,
        )

        chronicle_layer_selector_type_2.additional_properties = d
        return chronicle_layer_selector_type_2

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
