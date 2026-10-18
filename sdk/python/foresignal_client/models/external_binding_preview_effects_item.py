from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ExternalBindingPreviewEffectsItem")


@_attrs_define
class ExternalBindingPreviewEffectsItem:
    """
    Attributes:
        item_ref (str | Unset):
        action (str | Unset):
        target_ref (None | str | Unset):
    """

    item_ref: str | Unset = UNSET
    action: str | Unset = UNSET
    target_ref: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        item_ref = self.item_ref

        action = self.action

        target_ref: None | str | Unset
        if isinstance(self.target_ref, Unset):
            target_ref = UNSET
        else:
            target_ref = self.target_ref

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if item_ref is not UNSET:
            field_dict["itemRef"] = item_ref
        if action is not UNSET:
            field_dict["action"] = action
        if target_ref is not UNSET:
            field_dict["targetRef"] = target_ref

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        item_ref = d.pop("itemRef", UNSET)

        action = d.pop("action", UNSET)

        def _parse_target_ref(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        target_ref = _parse_target_ref(d.pop("targetRef", UNSET))

        external_binding_preview_effects_item = cls(
            item_ref=item_ref,
            action=action,
            target_ref=target_ref,
        )

        external_binding_preview_effects_item.additional_properties = d
        return external_binding_preview_effects_item

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
