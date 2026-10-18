from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.frame_stats import FrameStats


T = TypeVar("T", bound="RasterFrameObservation")


@_attrs_define
class RasterFrameObservation:
    """Observation variant: one version per frame

    Attributes:
        product_id (str):
        frame_key (str):
        time (datetime.datetime):
        time_key (str):
        tiles (str): Expanded content-addressed tile template
        bounds (list[float] | Unset): [west, south, east, north] in WGS84 degrees
        stats (FrameStats | Unset): Frame statistics in physical units
        expires_at (datetime.datetime | None | Unset):
    """

    product_id: str
    frame_key: str
    time: datetime.datetime
    time_key: str
    tiles: str
    bounds: list[float] | Unset = UNSET
    stats: FrameStats | Unset = UNSET
    expires_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        product_id = self.product_id

        frame_key = self.frame_key

        time = self.time.isoformat()

        time_key = self.time_key

        tiles = self.tiles

        bounds: list[float] | Unset = UNSET
        if not isinstance(self.bounds, Unset):
            bounds = self.bounds

        stats: dict[str, Any] | Unset = UNSET
        if not isinstance(self.stats, Unset):
            stats = self.stats.to_dict()

        expires_at: None | str | Unset
        if isinstance(self.expires_at, Unset):
            expires_at = UNSET
        elif isinstance(self.expires_at, datetime.datetime):
            expires_at = self.expires_at.isoformat()
        else:
            expires_at = self.expires_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "productId": product_id,
                "frameKey": frame_key,
                "time": time,
                "timeKey": time_key,
                "tiles": tiles,
            }
        )
        if bounds is not UNSET:
            field_dict["bounds"] = bounds
        if stats is not UNSET:
            field_dict["stats"] = stats
        if expires_at is not UNSET:
            field_dict["expiresAt"] = expires_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.frame_stats import FrameStats

        d = dict(src_dict)
        product_id = d.pop("productId")

        frame_key = d.pop("frameKey")

        time = datetime.datetime.fromisoformat(d.pop("time"))

        time_key = d.pop("timeKey")

        tiles = d.pop("tiles")

        bounds = cast(list[float], d.pop("bounds", UNSET))

        _stats = d.pop("stats", UNSET)
        stats: FrameStats | Unset
        if isinstance(_stats, Unset):
            stats = UNSET
        else:
            stats = FrameStats.from_dict(_stats)

        def _parse_expires_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                expires_at_type_0 = datetime.datetime.fromisoformat(data)

                return expires_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        expires_at = _parse_expires_at(d.pop("expiresAt", UNSET))

        raster_frame_observation = cls(
            product_id=product_id,
            frame_key=frame_key,
            time=time,
            time_key=time_key,
            tiles=tiles,
            bounds=bounds,
            stats=stats,
            expires_at=expires_at,
        )

        raster_frame_observation.additional_properties = d
        return raster_frame_observation

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
