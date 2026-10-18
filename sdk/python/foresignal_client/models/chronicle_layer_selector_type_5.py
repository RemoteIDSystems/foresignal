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

from ..types import UNSET, Unset

T = TypeVar("T", bound="ChronicleLayerSelectorType5")


@_attrs_define
class ChronicleLayerSelectorType5:
    """119 findings by type and family

    Attributes:
        type_ (Literal['findings']):
        finding_types (list[str] | Unset):
        families (list[str] | Unset):
    """

    type_: Literal["findings"]
    finding_types: list[str] | Unset = UNSET
    families: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_

        finding_types: list[str] | Unset = UNSET
        if not isinstance(self.finding_types, Unset):
            finding_types = self.finding_types

        families: list[str] | Unset = UNSET
        if not isinstance(self.families, Unset):
            families = self.families

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
            }
        )
        if finding_types is not UNSET:
            field_dict["findingTypes"] = finding_types
        if families is not UNSET:
            field_dict["families"] = families

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        type_ = cast(Literal["findings"], d.pop("type"))
        if type_ != "findings":
            raise ValueError(f"type must match const 'findings', got '{type_}'")

        finding_types = cast(list[str], d.pop("findingTypes", UNSET))

        families = cast(list[str], d.pop("families", UNSET))

        chronicle_layer_selector_type_5 = cls(
            type_=type_,
            finding_types=finding_types,
            families=families,
        )

        chronicle_layer_selector_type_5.additional_properties = d
        return chronicle_layer_selector_type_5

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
