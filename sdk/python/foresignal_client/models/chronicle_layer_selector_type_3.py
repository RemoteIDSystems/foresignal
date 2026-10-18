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

T = TypeVar("T", bound="ChronicleLayerSelectorType3")


@_attrs_define
class ChronicleLayerSelectorType3:
    """098 entities

    Attributes:
        type_ (Literal['entities']):
        entity_ids (list[str]):
    """

    type_: Literal["entities"]
    entity_ids: list[str]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_

        entity_ids = self.entity_ids

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
                "entityIds": entity_ids,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        type_ = cast(Literal["entities"], d.pop("type"))
        if type_ != "entities":
            raise ValueError(f"type must match const 'entities', got '{type_}'")

        entity_ids = cast(list[str], d.pop("entityIds"))

        chronicle_layer_selector_type_3 = cls(
            type_=type_,
            entity_ids=entity_ids,
        )

        chronicle_layer_selector_type_3.additional_properties = d
        return chronicle_layer_selector_type_3

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
