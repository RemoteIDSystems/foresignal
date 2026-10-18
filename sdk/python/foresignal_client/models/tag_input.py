from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.tag_input_namespace import TagInputNamespace
from ..models.tlp import Tlp
from ..models.visibility import Visibility
from ..types import UNSET, Unset

T = TypeVar("T", bound="TagInput")


@_attrs_define
class TagInput:
    """Create a tag; personal tags are visible only to their owner, shared tags default to `instance` visibility

    Attributes:
        name (str):
        namespace (TagInputNamespace | Unset):  Default: TagInputNamespace.PERSONAL.
        color (None | str | Unset):
        description (None | str | Unset):
        visibility (Visibility | Unset):
        tlp (Tlp | Unset): Traffic Light Protocol 2.0 label
    """

    name: str
    namespace: TagInputNamespace | Unset = TagInputNamespace.PERSONAL
    color: None | str | Unset = UNSET
    description: None | str | Unset = UNSET
    visibility: Visibility | Unset = UNSET
    tlp: Tlp | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        namespace: str | Unset = UNSET
        if not isinstance(self.namespace, Unset):
            namespace = self.namespace.value

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
        field_dict.update(
            {
                "name": name,
            }
        )
        if namespace is not UNSET:
            field_dict["namespace"] = namespace
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
        name = d.pop("name")

        _namespace = d.pop("namespace", UNSET)
        namespace: TagInputNamespace | Unset
        if isinstance(_namespace, Unset):
            namespace = UNSET
        else:
            namespace = TagInputNamespace(_namespace)

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

        tag_input = cls(
            name=name,
            namespace=namespace,
            color=color,
            description=description,
            visibility=visibility,
            tlp=tlp,
        )

        tag_input.additional_properties = d
        return tag_input

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
