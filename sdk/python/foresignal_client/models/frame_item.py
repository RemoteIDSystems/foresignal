from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.frame_item_props import FrameItemProps
    from ..models.geometry import Geometry


T = TypeVar("T", bound="FrameItem")


@_attrs_define
class FrameItem:
    """One entity in a history frame (037)

    Attributes:
        id (str): <sourceId>:<externalId>
        kind (str):
        observed_at (datetime.datetime):
        version (int):
        title (None | str | Unset):
        lon (float | None | Unset):
        lat (float | None | Unset):
        geometry (Geometry | None | Unset): Only with geometry=full
        props (FrameItemProps | Unset): tileProperties of the version
    """

    id: str
    kind: str
    observed_at: datetime.datetime
    version: int
    title: None | str | Unset = UNSET
    lon: float | None | Unset = UNSET
    lat: float | None | Unset = UNSET
    geometry: Geometry | None | Unset = UNSET
    props: FrameItemProps | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.geometry import Geometry

        id = self.id

        kind = self.kind

        observed_at = self.observed_at.isoformat()

        version = self.version

        title: None | str | Unset
        if isinstance(self.title, Unset):
            title = UNSET
        else:
            title = self.title

        lon: float | None | Unset
        if isinstance(self.lon, Unset):
            lon = UNSET
        else:
            lon = self.lon

        lat: float | None | Unset
        if isinstance(self.lat, Unset):
            lat = UNSET
        else:
            lat = self.lat

        geometry: dict[str, Any] | None | Unset
        if isinstance(self.geometry, Unset):
            geometry = UNSET
        elif isinstance(self.geometry, Geometry):
            geometry = self.geometry.to_dict()
        else:
            geometry = self.geometry

        props: dict[str, Any] | Unset = UNSET
        if not isinstance(self.props, Unset):
            props = self.props.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "kind": kind,
                "observedAt": observed_at,
                "version": version,
            }
        )
        if title is not UNSET:
            field_dict["title"] = title
        if lon is not UNSET:
            field_dict["lon"] = lon
        if lat is not UNSET:
            field_dict["lat"] = lat
        if geometry is not UNSET:
            field_dict["geometry"] = geometry
        if props is not UNSET:
            field_dict["props"] = props

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.frame_item_props import FrameItemProps
        from ..models.geometry import Geometry

        d = dict(src_dict)
        id = d.pop("id")

        kind = d.pop("kind")

        observed_at = datetime.datetime.fromisoformat(d.pop("observedAt"))

        version = d.pop("version")

        def _parse_title(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        title = _parse_title(d.pop("title", UNSET))

        def _parse_lon(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        lon = _parse_lon(d.pop("lon", UNSET))

        def _parse_lat(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        lat = _parse_lat(d.pop("lat", UNSET))

        def _parse_geometry(data: object) -> Geometry | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                geometry_type_0 = Geometry.from_dict(data)

                return geometry_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Geometry | None | Unset, data)

        geometry = _parse_geometry(d.pop("geometry", UNSET))

        _props = d.pop("props", UNSET)
        props: FrameItemProps | Unset
        if isinstance(_props, Unset):
            props = UNSET
        else:
            props = FrameItemProps.from_dict(_props)

        frame_item = cls(
            id=id,
            kind=kind,
            observed_at=observed_at,
            version=version,
            title=title,
            lon=lon,
            lat=lat,
            geometry=geometry,
            props=props,
        )

        frame_item.additional_properties = d
        return frame_item

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
