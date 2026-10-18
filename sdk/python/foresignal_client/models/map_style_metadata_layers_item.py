from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="MapStyleMetadataLayersItem")


@_attrs_define
class MapStyleMetadataLayersItem:
    """One requested tile layer

    Attributes:
        id (str):
        kinds (list[str]):
        attribution (str | Unset):
        minzoom (float | Unset):
        agg_below_zoom (float | None | Unset):
    """

    id: str
    kinds: list[str]
    attribution: str | Unset = UNSET
    minzoom: float | Unset = UNSET
    agg_below_zoom: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        kinds = self.kinds

        attribution = self.attribution

        minzoom = self.minzoom

        agg_below_zoom: float | None | Unset
        if isinstance(self.agg_below_zoom, Unset):
            agg_below_zoom = UNSET
        else:
            agg_below_zoom = self.agg_below_zoom

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "kinds": kinds,
            }
        )
        if attribution is not UNSET:
            field_dict["attribution"] = attribution
        if minzoom is not UNSET:
            field_dict["minzoom"] = minzoom
        if agg_below_zoom is not UNSET:
            field_dict["aggBelowZoom"] = agg_below_zoom

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        kinds = cast(list[str], d.pop("kinds"))

        attribution = d.pop("attribution", UNSET)

        minzoom = d.pop("minzoom", UNSET)

        def _parse_agg_below_zoom(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        agg_below_zoom = _parse_agg_below_zoom(d.pop("aggBelowZoom", UNSET))

        map_style_metadata_layers_item = cls(
            id=id,
            kinds=kinds,
            attribution=attribution,
            minzoom=minzoom,
            agg_below_zoom=agg_below_zoom,
        )

        map_style_metadata_layers_item.additional_properties = d
        return map_style_metadata_layers_item

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
