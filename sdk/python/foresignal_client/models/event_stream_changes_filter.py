from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="EventStreamChangesFilter")


@_attrs_define
class EventStreamChangesFilter:
    """Filter block for the `changes` topic of /v1/events/stream (same semantics as /v1/changes/stream)

    Attributes:
        source (list[str] | Unset): Source ids
        kind (list[str] | Unset): Kinds
        external_id (list[str] | Unset): External ids
        bbox (None | str | Unset): minLon,minLat,maxLon,maxLat; minLon > maxLon crosses the antimeridian
        near (None | str | Unset): `lon,lat`; requires radiusKm Example: -96.80,32.78.
        radius_km (float | None | Unset):
        live (bool | Unset): Send `live` events Default: True.
        touches (bool | Unset): Send `touch` events Default: False.
        payload (bool | Unset): Include payloads on `change` events Default: False.
        place (list[str] | Unset): Place ids (036): their areas (buffered by radiusKm when near is absent); needs a
            token
        view (None | str | Unset): Saved view id or slug (036); a view referencing a place or watchlist needs a token
        in_watchlist (None | Unset | UUID): Watchlist id (036); needs a token
    """

    source: list[str] | Unset = UNSET
    kind: list[str] | Unset = UNSET
    external_id: list[str] | Unset = UNSET
    bbox: None | str | Unset = UNSET
    near: None | str | Unset = UNSET
    radius_km: float | None | Unset = UNSET
    live: bool | Unset = True
    touches: bool | Unset = False
    payload: bool | Unset = False
    place: list[str] | Unset = UNSET
    view: None | str | Unset = UNSET
    in_watchlist: None | Unset | UUID = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source: list[str] | Unset = UNSET
        if not isinstance(self.source, Unset):
            source = self.source

        kind: list[str] | Unset = UNSET
        if not isinstance(self.kind, Unset):
            kind = self.kind

        external_id: list[str] | Unset = UNSET
        if not isinstance(self.external_id, Unset):
            external_id = self.external_id

        bbox: None | str | Unset
        if isinstance(self.bbox, Unset):
            bbox = UNSET
        else:
            bbox = self.bbox

        near: None | str | Unset
        if isinstance(self.near, Unset):
            near = UNSET
        else:
            near = self.near

        radius_km: float | None | Unset
        if isinstance(self.radius_km, Unset):
            radius_km = UNSET
        else:
            radius_km = self.radius_km

        live = self.live

        touches = self.touches

        payload = self.payload

        place: list[str] | Unset = UNSET
        if not isinstance(self.place, Unset):
            place = self.place

        view: None | str | Unset
        if isinstance(self.view, Unset):
            view = UNSET
        else:
            view = self.view

        in_watchlist: None | str | Unset
        if isinstance(self.in_watchlist, Unset):
            in_watchlist = UNSET
        elif isinstance(self.in_watchlist, UUID):
            in_watchlist = str(self.in_watchlist)
        else:
            in_watchlist = self.in_watchlist

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if source is not UNSET:
            field_dict["source"] = source
        if kind is not UNSET:
            field_dict["kind"] = kind
        if external_id is not UNSET:
            field_dict["externalId"] = external_id
        if bbox is not UNSET:
            field_dict["bbox"] = bbox
        if near is not UNSET:
            field_dict["near"] = near
        if radius_km is not UNSET:
            field_dict["radiusKm"] = radius_km
        if live is not UNSET:
            field_dict["live"] = live
        if touches is not UNSET:
            field_dict["touches"] = touches
        if payload is not UNSET:
            field_dict["payload"] = payload
        if place is not UNSET:
            field_dict["place"] = place
        if view is not UNSET:
            field_dict["view"] = view
        if in_watchlist is not UNSET:
            field_dict["inWatchlist"] = in_watchlist

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        source = cast(list[str], d.pop("source", UNSET))

        kind = cast(list[str], d.pop("kind", UNSET))

        external_id = cast(list[str], d.pop("externalId", UNSET))

        def _parse_bbox(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        bbox = _parse_bbox(d.pop("bbox", UNSET))

        def _parse_near(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        near = _parse_near(d.pop("near", UNSET))

        def _parse_radius_km(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        radius_km = _parse_radius_km(d.pop("radiusKm", UNSET))

        live = d.pop("live", UNSET)

        touches = d.pop("touches", UNSET)

        payload = d.pop("payload", UNSET)

        place = cast(list[str], d.pop("place", UNSET))

        def _parse_view(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        view = _parse_view(d.pop("view", UNSET))

        def _parse_in_watchlist(data: object) -> None | Unset | UUID:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                in_watchlist_type_0 = UUID(data)

                return in_watchlist_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Unset | UUID, data)

        in_watchlist = _parse_in_watchlist(d.pop("inWatchlist", UNSET))

        event_stream_changes_filter = cls(
            source=source,
            kind=kind,
            external_id=external_id,
            bbox=bbox,
            near=near,
            radius_km=radius_km,
            live=live,
            touches=touches,
            payload=payload,
            place=place,
            view=view,
            in_watchlist=in_watchlist,
        )

        event_stream_changes_filter.additional_properties = d
        return event_stream_changes_filter

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
