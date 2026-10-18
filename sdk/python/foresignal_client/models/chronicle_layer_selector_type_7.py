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

T = TypeVar("T", bound="ChronicleLayerSelectorType7")


@_attrs_define
class ChronicleLayerSelectorType7:
    """042 raster products

    Attributes:
        type_ (Literal['raster-products']):
        product_ids (list[str]):
    """

    type_: Literal["raster-products"]
    product_ids: list[str]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_

        product_ids = self.product_ids

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
                "productIds": product_ids,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        type_ = cast(Literal["raster-products"], d.pop("type"))
        if type_ != "raster-products":
            raise ValueError(f"type must match const 'raster-products', got '{type_}'")

        product_ids = cast(list[str], d.pop("productIds"))

        chronicle_layer_selector_type_7 = cls(
            type_=type_,
            product_ids=product_ids,
        )

        chronicle_layer_selector_type_7.additional_properties = d
        return chronicle_layer_selector_type_7

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
