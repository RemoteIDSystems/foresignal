from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry


T = TypeVar("T", bound="ReadRestriction")


@_attrs_define
class ReadRestriction:
    """The effective read restriction of a principal (035): restricted tokens, feed keys and the anonymous profile.
    Intersected with every request filter by `RestrictionClamp`; out-of-scope single reads return 404.

        Attributes:
            sources (list[str] | None | Unset): Allowed source ids (must exist); null = any
            kinds (list[str] | None | Unset): Allowed kinds (must exist); null = any
            bbox (list[float] | None | Unset): [minLon, minLat, maxLon, maxLat] (WGS84)
            endpoints (list[str] | None | Unset): Allowed operations as `METHOD /v1/path/pattern` (Ant-style; method may be
                `*`). Each pattern must match at least one spec operation the token's scope can reach; no match → 403
                ENDPOINT_NOT_ALLOWED (023) / RESTRICTED (035).
            federation (bool | Unset): Allows the federation feed endpoints (with read scope) Default: False.
            include_exercise (bool | Unset): Federation feed includes exercise records (115) Default: False.
            area (Geometry | None | Unset): MultiPolygon (WGS84, ≤ 2,000 vertices) limiting reads; combined with `bbox` when
                both are set
            place_ids (list[str] | None | Unset): Places (036) whose areas, unioned, bound what may be read (intersected
                with area and bbox); unknown places fail closed. Refused for the anonymous profile and feed keys (places are
                never anonymous)
            exclude_private (bool | Unset): Exclude private sources. Always true for anonymous callers and feed keys; for
                tokens the default leaves private visibility to the 023 gate (agent and admin tokens see private sources unless
                this is true) Default: True.
            max_requests_per_minute (int | None | Unset): Overrides the `read` rate-limit class for this principal
    """

    sources: list[str] | None | Unset = UNSET
    kinds: list[str] | None | Unset = UNSET
    bbox: list[float] | None | Unset = UNSET
    endpoints: list[str] | None | Unset = UNSET
    federation: bool | Unset = False
    include_exercise: bool | Unset = False
    area: Geometry | None | Unset = UNSET
    place_ids: list[str] | None | Unset = UNSET
    exclude_private: bool | Unset = True
    max_requests_per_minute: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

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

        endpoints: list[str] | None | Unset
        if isinstance(self.endpoints, Unset):
            endpoints = UNSET
        elif isinstance(self.endpoints, list):
            endpoints = self.endpoints

        else:
            endpoints = self.endpoints

        federation = self.federation

        include_exercise = self.include_exercise

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

        exclude_private = self.exclude_private

        max_requests_per_minute: int | None | Unset
        if isinstance(self.max_requests_per_minute, Unset):
            max_requests_per_minute = UNSET
        else:
            max_requests_per_minute = self.max_requests_per_minute

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if sources is not UNSET:
            field_dict["sources"] = sources
        if kinds is not UNSET:
            field_dict["kinds"] = kinds
        if bbox is not UNSET:
            field_dict["bbox"] = bbox
        if endpoints is not UNSET:
            field_dict["endpoints"] = endpoints
        if federation is not UNSET:
            field_dict["federation"] = federation
        if include_exercise is not UNSET:
            field_dict["includeExercise"] = include_exercise
        if area is not UNSET:
            field_dict["area"] = area
        if place_ids is not UNSET:
            field_dict["placeIds"] = place_ids
        if exclude_private is not UNSET:
            field_dict["excludePrivate"] = exclude_private
        if max_requests_per_minute is not UNSET:
            field_dict["maxRequestsPerMinute"] = max_requests_per_minute

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

        def _parse_endpoints(data: object) -> list[str] | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, list):
                    raise TypeError()
                endpoints_type_0 = cast(list[str], data)

                return endpoints_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(list[str] | None | Unset, data)

        endpoints = _parse_endpoints(d.pop("endpoints", UNSET))

        federation = d.pop("federation", UNSET)

        include_exercise = d.pop("includeExercise", UNSET)

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

        exclude_private = d.pop("excludePrivate", UNSET)

        def _parse_max_requests_per_minute(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        max_requests_per_minute = _parse_max_requests_per_minute(
            d.pop("maxRequestsPerMinute", UNSET)
        )

        read_restriction = cls(
            sources=sources,
            kinds=kinds,
            bbox=bbox,
            endpoints=endpoints,
            federation=federation,
            include_exercise=include_exercise,
            area=area,
            place_ids=place_ids,
            exclude_private=exclude_private,
            max_requests_per_minute=max_requests_per_minute,
        )

        read_restriction.additional_properties = d
        return read_restriction

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
