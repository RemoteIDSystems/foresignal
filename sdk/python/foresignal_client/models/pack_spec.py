from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.pack_spec_raster_type_0 import PackSpecRasterType0
    from ..models.pack_spec_vector_type_0 import PackSpecVectorType0


T = TypeVar("T", bound="PackSpec")


@_attrs_define
class PackSpec:
    """PMTiles v3 offline pack request: one .pmtiles per tile type (MVT and/or PNG) (111)

    Attributes:
        name (str):
        min_zoom (int):
        max_zoom (int):
        bbox (list[float] | None | Unset): [minLon, minLat, maxLon, maxLat] (WGS84)
        place_id (None | str | Unset):
        vector (None | PackSpecVectorType0 | Unset):
        raster (None | PackSpecRasterType0 | Unset):
        include_private (bool | Unset): Admin only Default: False.
        personal_use (bool | Unset): As ExportSpec.personalUse Default: False.
    """

    name: str
    min_zoom: int
    max_zoom: int
    bbox: list[float] | None | Unset = UNSET
    place_id: None | str | Unset = UNSET
    vector: None | PackSpecVectorType0 | Unset = UNSET
    raster: None | PackSpecRasterType0 | Unset = UNSET
    include_private: bool | Unset = False
    personal_use: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.pack_spec_raster_type_0 import PackSpecRasterType0
        from ..models.pack_spec_vector_type_0 import PackSpecVectorType0

        name = self.name

        min_zoom = self.min_zoom

        max_zoom = self.max_zoom

        bbox: list[float] | None | Unset
        if isinstance(self.bbox, Unset):
            bbox = UNSET
        elif isinstance(self.bbox, list):
            bbox = self.bbox

        else:
            bbox = self.bbox

        place_id: None | str | Unset
        if isinstance(self.place_id, Unset):
            place_id = UNSET
        else:
            place_id = self.place_id

        vector: dict[str, Any] | None | Unset
        if isinstance(self.vector, Unset):
            vector = UNSET
        elif isinstance(self.vector, PackSpecVectorType0):
            vector = self.vector.to_dict()
        else:
            vector = self.vector

        raster: dict[str, Any] | None | Unset
        if isinstance(self.raster, Unset):
            raster = UNSET
        elif isinstance(self.raster, PackSpecRasterType0):
            raster = self.raster.to_dict()
        else:
            raster = self.raster

        include_private = self.include_private

        personal_use = self.personal_use

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "minZoom": min_zoom,
                "maxZoom": max_zoom,
            }
        )
        if bbox is not UNSET:
            field_dict["bbox"] = bbox
        if place_id is not UNSET:
            field_dict["placeId"] = place_id
        if vector is not UNSET:
            field_dict["vector"] = vector
        if raster is not UNSET:
            field_dict["raster"] = raster
        if include_private is not UNSET:
            field_dict["includePrivate"] = include_private
        if personal_use is not UNSET:
            field_dict["personalUse"] = personal_use

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.pack_spec_raster_type_0 import PackSpecRasterType0
        from ..models.pack_spec_vector_type_0 import PackSpecVectorType0

        d = dict(src_dict)
        name = d.pop("name")

        min_zoom = d.pop("minZoom")

        max_zoom = d.pop("maxZoom")

        def _parse_bbox(data: object) -> list[float] | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, list):
                    raise TypeError()
                bbox_type_0 = cast(list[float], data)

                return bbox_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(list[float] | None | Unset, data)

        bbox = _parse_bbox(d.pop("bbox", UNSET))

        def _parse_place_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        place_id = _parse_place_id(d.pop("placeId", UNSET))

        def _parse_vector(data: object) -> None | PackSpecVectorType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                vector_type_0 = PackSpecVectorType0.from_dict(data)

                return vector_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | PackSpecVectorType0 | Unset, data)

        vector = _parse_vector(d.pop("vector", UNSET))

        def _parse_raster(data: object) -> None | PackSpecRasterType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                raster_type_0 = PackSpecRasterType0.from_dict(data)

                return raster_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | PackSpecRasterType0 | Unset, data)

        raster = _parse_raster(d.pop("raster", UNSET))

        include_private = d.pop("includePrivate", UNSET)

        personal_use = d.pop("personalUse", UNSET)

        pack_spec = cls(
            name=name,
            min_zoom=min_zoom,
            max_zoom=max_zoom,
            bbox=bbox,
            place_id=place_id,
            vector=vector,
            raster=raster,
            include_private=include_private,
            personal_use=personal_use,
        )

        pack_spec.additional_properties = d
        return pack_spec

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
