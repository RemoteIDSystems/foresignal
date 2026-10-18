from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SinkFilter")


@_attrs_define
class SinkFilter:
    """Which records a sink receives (110)

    Attributes:
        sources (list[str] | Unset):
        kinds (list[str] | Unset):
        bbox (list[float] | None | Unset): [minLon, minLat, maxLon, maxLat] (WGS84)
        place_id (None | str | Unset): Place (036) whose geometry filters records
        watchlist_id (None | str | Unset): Watchlist (036) whose members are delivered
        include_private (bool | Unset): Include private sources (039); allowed only for SERVE on loopback or a PUSH
            target that resolves to a private address Default: False.
    """

    sources: list[str] | Unset = UNSET
    kinds: list[str] | Unset = UNSET
    bbox: list[float] | None | Unset = UNSET
    place_id: None | str | Unset = UNSET
    watchlist_id: None | str | Unset = UNSET
    include_private: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        sources: list[str] | Unset = UNSET
        if not isinstance(self.sources, Unset):
            sources = self.sources

        kinds: list[str] | Unset = UNSET
        if not isinstance(self.kinds, Unset):
            kinds = self.kinds

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

        watchlist_id: None | str | Unset
        if isinstance(self.watchlist_id, Unset):
            watchlist_id = UNSET
        else:
            watchlist_id = self.watchlist_id

        include_private = self.include_private

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if sources is not UNSET:
            field_dict["sources"] = sources
        if kinds is not UNSET:
            field_dict["kinds"] = kinds
        if bbox is not UNSET:
            field_dict["bbox"] = bbox
        if place_id is not UNSET:
            field_dict["placeId"] = place_id
        if watchlist_id is not UNSET:
            field_dict["watchlistId"] = watchlist_id
        if include_private is not UNSET:
            field_dict["includePrivate"] = include_private

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        sources = cast(list[str], d.pop("sources", UNSET))

        kinds = cast(list[str], d.pop("kinds", UNSET))

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

        def _parse_watchlist_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        watchlist_id = _parse_watchlist_id(d.pop("watchlistId", UNSET))

        include_private = d.pop("includePrivate", UNSET)

        sink_filter = cls(
            sources=sources,
            kinds=kinds,
            bbox=bbox,
            place_id=place_id,
            watchlist_id=watchlist_id,
            include_private=include_private,
        )

        sink_filter.additional_properties = d
        return sink_filter

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
