from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define

from ..types import UNSET, Unset

T = TypeVar("T", bound="RecordMappingGeometryType1")


@_attrs_define
class RecordMappingGeometryType1:
    """
    Attributes:
        geojson (str): JSONPath of a GeoJSON geometry
        fallback (list[float] | Unset): [lon, lat] used when lon/lat (or geojson) give no geometry
    """

    geojson: str
    fallback: list[float] | Unset = UNSET

    def to_dict(self) -> dict[str, Any]:
        geojson = self.geojson

        fallback: list[float] | Unset = UNSET
        if not isinstance(self.fallback, Unset):
            fallback = self.fallback

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "geojson": geojson,
            }
        )
        if fallback is not UNSET:
            field_dict["fallback"] = fallback

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        geojson = d.pop("geojson")

        fallback = cast(list[float], d.pop("fallback", UNSET))

        record_mapping_geometry_type_1 = cls(
            geojson=geojson,
            fallback=fallback,
        )

        return record_mapping_geometry_type_1
