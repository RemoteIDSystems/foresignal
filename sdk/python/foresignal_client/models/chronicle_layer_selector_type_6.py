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

T = TypeVar("T", bound="ChronicleLayerSelectorType6")


@_attrs_define
class ChronicleLayerSelectorType6:
    """Series refs (119 registry)

    Attributes:
        type_ (Literal['series']):
        refs (list[str]):
    """

    type_: Literal["series"]
    refs: list[str]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_

        refs = self.refs

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
                "refs": refs,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        type_ = cast(Literal["series"], d.pop("type"))
        if type_ != "series":
            raise ValueError(f"type must match const 'series', got '{type_}'")

        refs = cast(list[str], d.pop("refs"))

        chronicle_layer_selector_type_6 = cls(
            type_=type_,
            refs=refs,
        )

        chronicle_layer_selector_type_6.additional_properties = d
        return chronicle_layer_selector_type_6

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
