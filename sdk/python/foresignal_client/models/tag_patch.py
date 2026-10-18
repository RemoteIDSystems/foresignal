from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.tlp import Tlp
from ..models.visibility import Visibility
from ..types import UNSET, Unset

T = TypeVar("T", bound="TagPatch")


@_attrs_define
class TagPatch:
    """JSON merge patch for a tag (owner or editor)

    Attributes:
        name (str | Unset):
        color (None | str | Unset):
        description (None | str | Unset):
        visibility (Visibility | Unset):
        tlp (Tlp | Unset): Traffic Light Protocol 2.0 label
    """

    name: str | Unset = UNSET
    color: None | str | Unset = UNSET
    description: None | str | Unset = UNSET
    visibility: Visibility | Unset = UNSET
    tlp: Tlp | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        color: None | str | Unset
        if isinstance(self.color, Unset):
            color = UNSET
        else:
            color = self.color

        description: None | str | Unset
        if isinstance(self.description, Unset):
            description = UNSET
        else:
            description = self.description

        visibility: str | Unset = UNSET
        if not isinstance(self.visibility, Unset):
            visibility = self.visibility.value

        tlp: str | Unset = UNSET
        if not isinstance(self.tlp, Unset):
            tlp = self.tlp.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if name is not UNSET:
            field_dict["name"] = name
        if color is not UNSET:
            field_dict["color"] = color
        if description is not UNSET:
            field_dict["description"] = description
        if visibility is not UNSET:
            field_dict["visibility"] = visibility
        if tlp is not UNSET:
            field_dict["tlp"] = tlp

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        name = d.pop("name", UNSET)

        def _parse_color(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        color = _parse_color(d.pop("color", UNSET))

        def _parse_description(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        description = _parse_description(d.pop("description", UNSET))

        _visibility = d.pop("visibility", UNSET)
        visibility: Visibility | Unset
        if isinstance(_visibility, Unset):
            visibility = UNSET
        else:
            visibility = Visibility(_visibility)

        _tlp = d.pop("tlp", UNSET)
        tlp: Tlp | Unset
        if isinstance(_tlp, Unset):
            tlp = UNSET
        else:
            tlp = Tlp(_tlp)

        tag_patch = cls(
            name=name,
            color=color,
            description=description,
            visibility=visibility,
            tlp=tlp,
        )

        tag_patch.additional_properties = d
        return tag_patch

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
