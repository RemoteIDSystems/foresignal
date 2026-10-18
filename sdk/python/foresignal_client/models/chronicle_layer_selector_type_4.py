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

T = TypeVar("T", bound="ChronicleLayerSelectorType4")


@_attrs_define
class ChronicleLayerSelectorType4:
    """118 situations (membership-driven)

    Attributes:
        type_ (Literal['situations']):
        situation_ids (list[str]):
    """

    type_: Literal["situations"]
    situation_ids: list[str]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_

        situation_ids = self.situation_ids

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
                "situationIds": situation_ids,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        type_ = cast(Literal["situations"], d.pop("type"))
        if type_ != "situations":
            raise ValueError(f"type must match const 'situations', got '{type_}'")

        situation_ids = cast(list[str], d.pop("situationIds"))

        chronicle_layer_selector_type_4 = cls(
            type_=type_,
            situation_ids=situation_ids,
        )

        chronicle_layer_selector_type_4.additional_properties = d
        return chronicle_layer_selector_type_4

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
