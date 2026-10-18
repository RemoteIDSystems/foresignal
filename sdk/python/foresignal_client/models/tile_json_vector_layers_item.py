from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.tile_json_vector_layers_item_fields import (
        TileJsonVectorLayersItemFields,
    )


T = TypeVar("T", bound="TileJsonVectorLayersItem")


@_attrs_define
class TileJsonVectorLayersItem:
    """
    Attributes:
        id (str):
        fields (TileJsonVectorLayersItemFields): Property name → description (fieldSpecs label and unit, else
            String/Number)
        description (str | Unset):
        minzoom (int | Unset):
        maxzoom (int | Unset):
    """

    id: str
    fields: TileJsonVectorLayersItemFields
    description: str | Unset = UNSET
    minzoom: int | Unset = UNSET
    maxzoom: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        fields = self.fields.to_dict()

        description = self.description

        minzoom = self.minzoom

        maxzoom = self.maxzoom

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "fields": fields,
            }
        )
        if description is not UNSET:
            field_dict["description"] = description
        if minzoom is not UNSET:
            field_dict["minzoom"] = minzoom
        if maxzoom is not UNSET:
            field_dict["maxzoom"] = maxzoom

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.tile_json_vector_layers_item_fields import (
            TileJsonVectorLayersItemFields,
        )

        d = dict(src_dict)
        id = d.pop("id")

        fields = TileJsonVectorLayersItemFields.from_dict(d.pop("fields"))

        description = d.pop("description", UNSET)

        minzoom = d.pop("minzoom", UNSET)

        maxzoom = d.pop("maxzoom", UNSET)

        tile_json_vector_layers_item = cls(
            id=id,
            fields=fields,
            description=description,
            minzoom=minzoom,
            maxzoom=maxzoom,
        )

        tile_json_vector_layers_item.additional_properties = d
        return tile_json_vector_layers_item

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
