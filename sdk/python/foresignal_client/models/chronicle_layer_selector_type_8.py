from __future__ import annotations

from collections.abc import Mapping
from typing import (
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ChronicleLayerSelectorType8")


@_attrs_define
class ChronicleLayerSelectorType8:
    """Alerts by rule ids or tags

    Attributes:
        type_ (Literal['alerts']):
        rule_ids (list[UUID] | Unset):
        tags (list[str] | Unset):
    """

    type_: Literal["alerts"]
    rule_ids: list[UUID] | Unset = UNSET
    tags: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_

        rule_ids: list[str] | Unset = UNSET
        if not isinstance(self.rule_ids, Unset):
            rule_ids = []
            for rule_ids_item_data in self.rule_ids:
                rule_ids_item = str(rule_ids_item_data)
                rule_ids.append(rule_ids_item)

        tags: list[str] | Unset = UNSET
        if not isinstance(self.tags, Unset):
            tags = self.tags

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
            }
        )
        if rule_ids is not UNSET:
            field_dict["ruleIds"] = rule_ids
        if tags is not UNSET:
            field_dict["tags"] = tags

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        type_ = cast(Literal["alerts"], d.pop("type"))
        if type_ != "alerts":
            raise ValueError(f"type must match const 'alerts', got '{type_}'")

        _rule_ids = d.pop("ruleIds", UNSET)
        rule_ids: list[UUID] | Unset = UNSET
        if _rule_ids is not UNSET:
            rule_ids = []
            for rule_ids_item_data in _rule_ids:
                rule_ids_item = UUID(rule_ids_item_data)

                rule_ids.append(rule_ids_item)

        tags = cast(list[str], d.pop("tags", UNSET))

        chronicle_layer_selector_type_8 = cls(
            type_=type_,
            rule_ids=rule_ids,
            tags=tags,
        )

        chronicle_layer_selector_type_8.additional_properties = d
        return chronicle_layer_selector_type_8

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
