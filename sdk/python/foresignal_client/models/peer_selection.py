from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="PeerSelection")


@_attrs_define
class PeerSelection:
    """Selective replication from a peer; remote filters are passed as query parameters and re-checked locally (113)

    Attributes:
        sources (list[str] | Unset):
        kinds (list[str] | Unset):
        bbox (list[float] | None | Unset): [minLon, minLat, maxLon, maxLat] (WGS84)
        place_id (None | str | Unset):
        max_age (None | str | Unset): Drop items whose observedAt is older than this. ISO-8601 duration (P7D, PT15M) or
            shorthand (7d, 15m, 12h). Example: 15m.
        backfill (str | Unset): `none` (current snapshot only) or a duration of history to page from /changes Default:
            'none'.
        live (bool | Unset): Also receive the live lane Default: False.
        max_hops (int | Unset):  Default: 3.
        track_ttl (str | Unset): TTL of peer-{id}-trk when the remote kind's TTL is unknown. ISO-8601 duration (P7D,
            PT15M) or shorthand (7d, 15m, 12h). Default: '10m'. Example: 10m.
    """

    sources: list[str] | Unset = UNSET
    kinds: list[str] | Unset = UNSET
    bbox: list[float] | None | Unset = UNSET
    place_id: None | str | Unset = UNSET
    max_age: None | str | Unset = UNSET
    backfill: str | Unset = "none"
    live: bool | Unset = False
    max_hops: int | Unset = 3
    track_ttl: str | Unset = "10m"
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

        max_age: None | str | Unset
        if isinstance(self.max_age, Unset):
            max_age = UNSET
        else:
            max_age = self.max_age

        backfill = self.backfill

        live = self.live

        max_hops = self.max_hops

        track_ttl = self.track_ttl

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
        if max_age is not UNSET:
            field_dict["maxAge"] = max_age
        if backfill is not UNSET:
            field_dict["backfill"] = backfill
        if live is not UNSET:
            field_dict["live"] = live
        if max_hops is not UNSET:
            field_dict["maxHops"] = max_hops
        if track_ttl is not UNSET:
            field_dict["trackTtl"] = track_ttl

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

        def _parse_max_age(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        max_age = _parse_max_age(d.pop("maxAge", UNSET))

        backfill = d.pop("backfill", UNSET)

        live = d.pop("live", UNSET)

        max_hops = d.pop("maxHops", UNSET)

        track_ttl = d.pop("trackTtl", UNSET)

        peer_selection = cls(
            sources=sources,
            kinds=kinds,
            bbox=bbox,
            place_id=place_id,
            max_age=max_age,
            backfill=backfill,
            live=live,
            max_hops=max_hops,
            track_ttl=track_ttl,
        )

        peer_selection.additional_properties = d
        return peer_selection

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
