from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ProductRender")


@_attrs_define
class ProductRender:
    """Rendering defaults

    Attributes:
        default_style (str): Versioned default style Example: nws-reflectivity@1.
        styles (list[str]): Versioned styles valid for this product
        default_opacity (float | Unset):
    """

    default_style: str
    styles: list[str]
    default_opacity: float | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        default_style = self.default_style

        styles = self.styles

        default_opacity = self.default_opacity

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "defaultStyle": default_style,
                "styles": styles,
            }
        )
        if default_opacity is not UNSET:
            field_dict["defaultOpacity"] = default_opacity

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        default_style = d.pop("defaultStyle")

        styles = cast(list[str], d.pop("styles"))

        default_opacity = d.pop("defaultOpacity", UNSET)

        product_render = cls(
            default_style=default_style,
            styles=styles,
            default_opacity=default_opacity,
        )

        product_render.additional_properties = d
        return product_render

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
