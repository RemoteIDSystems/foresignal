from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.external_binding_preview_effects_item import (
        ExternalBindingPreviewEffectsItem,
    )


T = TypeVar("T", bound="ExternalBindingPreview")


@_attrs_define
class ExternalBindingPreview:
    """What a binding would do over recent items (no side effects)

    Attributes:
        evaluated (int):
        matched (int):
        effects (list[ExternalBindingPreviewEffectsItem]):
        errors (list[str] | Unset):
    """

    evaluated: int
    matched: int
    effects: list[ExternalBindingPreviewEffectsItem]
    errors: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        evaluated = self.evaluated

        matched = self.matched

        effects = []
        for effects_item_data in self.effects:
            effects_item = effects_item_data.to_dict()
            effects.append(effects_item)

        errors: list[str] | Unset = UNSET
        if not isinstance(self.errors, Unset):
            errors = self.errors

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "evaluated": evaluated,
                "matched": matched,
                "effects": effects,
            }
        )
        if errors is not UNSET:
            field_dict["errors"] = errors

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.external_binding_preview_effects_item import (
            ExternalBindingPreviewEffectsItem,
        )

        d = dict(src_dict)
        evaluated = d.pop("evaluated")

        matched = d.pop("matched")

        effects = []
        _effects = d.pop("effects")
        for effects_item_data in _effects:
            effects_item = ExternalBindingPreviewEffectsItem.from_dict(
                effects_item_data
            )

            effects.append(effects_item)

        errors = cast(list[str], d.pop("errors", UNSET))

        external_binding_preview = cls(
            evaluated=evaluated,
            matched=matched,
            effects=effects,
            errors=errors,
        )

        external_binding_preview.additional_properties = d
        return external_binding_preview

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
