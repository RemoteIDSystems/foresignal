from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="TokenRestrictions")


@_attrs_define
class TokenRestrictions:
    """Restrictions stored on an API token, feed key or pairing code (023, 035). Endpoint restrictions are enforced by the
    token filter (403 ENDPOINT_NOT_ALLOWED); source, kind, bbox and area restrictions (see ReadRestriction) are applied
    on every read path from 035 by RestrictionClamp.

        Attributes:
            sources (list[str] | None | Unset): Allowed source ids (must exist); null = any
            kinds (list[str] | None | Unset): Allowed kinds (must exist); null = any
            bbox (list[float] | None | Unset): [minLon, minLat, maxLon, maxLat] (WGS84)
            endpoints (list[str] | None | Unset): Allowed operations as `METHOD /v1/path/pattern` (Ant-style; method may be
                `*`). Each pattern must match at least one spec operation the token's scope can reach; no match → 403
                ENDPOINT_NOT_ALLOWED (023) / RESTRICTED (035).
            federation (bool | Unset): Allows the federation feed endpoints (with read scope) Default: False.
            include_exercise (bool | Unset): Federation feed includes exercise records (115) Default: False.
    """

    sources: list[str] | None | Unset = UNSET
    kinds: list[str] | None | Unset = UNSET
    bbox: list[float] | None | Unset = UNSET
    endpoints: list[str] | None | Unset = UNSET
    federation: bool | Unset = False
    include_exercise: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
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

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
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

        token_restrictions = cls(
            sources=sources,
            kinds=kinds,
            bbox=bbox,
            endpoints=endpoints,
            federation=federation,
            include_exercise=include_exercise,
        )

        token_restrictions.additional_properties = d
        return token_restrictions

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
