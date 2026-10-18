from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.map_sprite_index_additional_property import (
        MapSpriteIndexAdditionalProperty,
    )


T = TypeVar("T", bound="MapSpriteIndex")


@_attrs_define
class MapSpriteIndex:
    """MapLibre sprite index (`foresignal-{sha8}[@2x].json`): icon name → position in the sheet; all icons are SDF (058)"""

    additional_properties: dict[str, MapSpriteIndexAdditionalProperty] = _attrs_field(
        init=False, factory=dict
    )

    def to_dict(self) -> dict[str, Any]:

        field_dict: dict[str, Any] = {}
        for prop_name, prop in self.additional_properties.items():
            field_dict[prop_name] = prop.to_dict()

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.map_sprite_index_additional_property import (
            MapSpriteIndexAdditionalProperty,
        )

        d = dict(src_dict)
        map_sprite_index = cls()

        additional_properties = {}
        for prop_name, prop_dict in d.items():
            additional_property = MapSpriteIndexAdditionalProperty.from_dict(prop_dict)

            additional_properties[prop_name] = additional_property

        map_sprite_index.additional_properties = additional_properties
        return map_sprite_index

    @property
    def additional_keys(self) -> list[str]:
        return list(self.additional_properties.keys())

    def __getitem__(self, key: str) -> MapSpriteIndexAdditionalProperty:
        return self.additional_properties[key]

    def __setitem__(self, key: str, value: MapSpriteIndexAdditionalProperty) -> None:
        self.additional_properties[key] = value

    def __delitem__(self, key: str) -> None:
        del self.additional_properties[key]

    def __contains__(self, key: str) -> bool:
        return key in self.additional_properties
