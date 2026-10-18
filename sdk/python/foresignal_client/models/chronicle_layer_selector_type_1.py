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

T = TypeVar("T", bound="ChronicleLayerSelectorType1")


@_attrs_define
class ChronicleLayerSelectorType1:
    """A 036 saved view

    Attributes:
        type_ (Literal['saved-view']):
        view_id (str):
    """

    type_: Literal["saved-view"]
    view_id: str
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_

        view_id = self.view_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
                "viewId": view_id,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        type_ = cast(Literal["saved-view"], d.pop("type"))
        if type_ != "saved-view":
            raise ValueError(f"type must match const 'saved-view', got '{type_}'")

        view_id = d.pop("viewId")

        chronicle_layer_selector_type_1 = cls(
            type_=type_,
            view_id=view_id,
        )

        chronicle_layer_selector_type_1.additional_properties = d
        return chronicle_layer_selector_type_1

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
