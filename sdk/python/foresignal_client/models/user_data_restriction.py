from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry


T = TypeVar("T", bound="UserDataRestriction")


@_attrs_define
class UserDataRestriction:
    """What a user may read (R2-013): the data members of a token's ReadRestriction, stored per user and set by an admin
    through updateUser. Null members mean any; a session's reads are intersected with it through the same clamp as a
    restricted token's.

        Attributes:
            sources (list[str] | None | Unset): Allowed source ids (must exist); null = any
            kinds (list[str] | None | Unset): Allowed kinds (must exist); null = any
            bbox (list[float] | None | Unset): [minLon, minLat, maxLon, maxLat] (WGS84)
            area (Geometry | None | Unset): MultiPolygon (WGS84, ≤ 2,000 vertices); combined with `bbox` when both are set
            place_ids (list[str] | None | Unset): Places whose areas, unioned, bound what may be read; unknown places fail
                closed
    """

    sources: list[str] | None | Unset = UNSET
    kinds: list[str] | None | Unset = UNSET
    bbox: list[float] | None | Unset = UNSET
    area: Geometry | None | Unset = UNSET
    place_ids: list[str] | None | Unset = UNSET

    def to_dict(self) -> dict[str, Any]:
        from ..models.geometry import Geometry

        sources: list[str] | None | Unset
        if isinstance(self.sources, Unset):
            sources = UNSET
        elif isinstance(self.sources, list):
            sources = self.sources

        else:
            sources = self.sources

        kinds: list[str] | None | Unset
        if isinstance(self.kinds, Unset):
            kinds = UNSET
        elif isinstance(self.kinds, list):
            kinds = self.kinds

        else:
            kinds = self.kinds

        bbox: list[float] | None | Unset
        if isinstance(self.bbox, Unset):
            bbox = UNSET
        elif isinstance(self.bbox, list):
            bbox = self.bbox

        else:
            bbox = self.bbox

        area: dict[str, Any] | None | Unset
        if isinstance(self.area, Unset):
            area = UNSET
        elif isinstance(self.area, Geometry):
            area = self.area.to_dict()
        else:
            area = self.area

        place_ids: list[str] | None | Unset
        if isinstance(self.place_ids, Unset):
            place_ids = UNSET
        elif isinstance(self.place_ids, list):
            place_ids = self.place_ids

        else:
            place_ids = self.place_ids

        field_dict: dict[str, Any] = {}

        field_dict.update({})
        if sources is not UNSET:
            field_dict["sources"] = sources
        if kinds is not UNSET:
            field_dict["kinds"] = kinds
        if bbox is not UNSET:
            field_dict["bbox"] = bbox
        if area is not UNSET:
            field_dict["area"] = area
        if place_ids is not UNSET:
            field_dict["placeIds"] = place_ids

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry

        d = dict(src_dict)

        def _parse_sources(data: object) -> list[str] | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, list):
                    raise TypeError()
                sources_type_0 = cast(list[str], data)

                return sources_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(list[str] | None | Unset, data)

        sources = _parse_sources(d.pop("sources", UNSET))

        def _parse_kinds(data: object) -> list[str] | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, list):
                    raise TypeError()
                kinds_type_0 = cast(list[str], data)

                return kinds_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(list[str] | None | Unset, data)

        kinds = _parse_kinds(d.pop("kinds", UNSET))

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

        def _parse_area(data: object) -> Geometry | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                area_type_0 = Geometry.from_dict(data)

                return area_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Geometry | None | Unset, data)

        area = _parse_area(d.pop("area", UNSET))

        def _parse_place_ids(data: object) -> list[str] | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, list):
                    raise TypeError()
                place_ids_type_0 = cast(list[str], data)

                return place_ids_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(list[str] | None | Unset, data)

        place_ids = _parse_place_ids(d.pop("placeIds", UNSET))

        user_data_restriction = cls(
            sources=sources,
            kinds=kinds,
            bbox=bbox,
            area=area,
            place_ids=place_ids,
        )

        return user_data_restriction
