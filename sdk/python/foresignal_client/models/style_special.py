from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.style_special_kind import StyleSpecialKind

T = TypeVar("T", bound="StyleSpecial")


@_attrs_define
class StyleSpecial:
    """Special value rendering

    Attributes:
        kind (StyleSpecialKind):
        color (str): `transparent` or a colour
        label (str):
    """

    kind: StyleSpecialKind
    color: str
    label: str
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        kind = self.kind.value

        color = self.color

        label = self.label

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "kind": kind,
                "color": color,
                "label": label,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        kind = StyleSpecialKind(d.pop("kind"))

        color = d.pop("color")

        label = d.pop("label")

        style_special = cls(
            kind=kind,
            color=color,
            label=label,
        )

        style_special.additional_properties = d
        return style_special

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
