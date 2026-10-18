from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.raster_frame_status import RasterFrameStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.frame_stats import FrameStats


T = TypeVar("T", bound="RasterFrame")


@_attrs_define
class RasterFrame:
    """One frame of a product with expanded URLs (Weather-Imagery-Design §6.1)

    Attributes:
        product_id (str):
        time (datetime.datetime): Valid time (ISO extended UTC)
        time_key (str): Path form of the valid time Example: 20261010T143240Z.
        frame_key (str): Content-addressed key `{timeKey}~{sha8}` (sha8 = first 8 hex chars of the decoded-content
            sha256) Example: 20261010T143240Z~1a2b3c4d.
        status (RasterFrameStatus):
        tiles (str): Expanded content-addressed tile template with `{z}/{x}/{y}` Example:
            https://fs.local/v1/products/mrms-reflectivity/tiles/20261010T143240Z~1a2b3c4d/{z}/{x}/{y}.png?style=nws-
            reflectivity@1.
        expires_at (datetime.datetime | None | Unset): When retention removes the frame; drop it client-side after this
        run (None | str | Unset): Forecast run reference time (ISO-8601); null for observations
        step (int | None | Unset): Forecast step in hours; null for observations
        obs_start (datetime.datetime | None | Unset):
        obs_end (datetime.datetime | None | Unset):
        tiles2x (str | Unset): Same as `tiles` with the `@2x` (512 px) suffix
        image (str | Unset): Expanded `/image` template with `{bbox}`, `{width}`, `{height}` placeholders
        bounds (list[float] | None | Unset): [west, south, east, north] in WGS84 degrees
        stats (FrameStats | Unset): Frame statistics in physical units
        thinned (bool | Unset): Kept by retention-ladder thinning
    """

    product_id: str
    time: datetime.datetime
    time_key: str
    frame_key: str
    status: RasterFrameStatus
    tiles: str
    expires_at: datetime.datetime | None | Unset = UNSET
    run: None | str | Unset = UNSET
    step: int | None | Unset = UNSET
    obs_start: datetime.datetime | None | Unset = UNSET
    obs_end: datetime.datetime | None | Unset = UNSET
    tiles2x: str | Unset = UNSET
    image: str | Unset = UNSET
    bounds: list[float] | None | Unset = UNSET
    stats: FrameStats | Unset = UNSET
    thinned: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        product_id = self.product_id

        time = self.time.isoformat()

        time_key = self.time_key

        frame_key = self.frame_key

        status = self.status.value

        tiles = self.tiles

        expires_at: None | str | Unset
        if isinstance(self.expires_at, Unset):
            expires_at = UNSET
        elif isinstance(self.expires_at, datetime.datetime):
            expires_at = self.expires_at.isoformat()
        else:
            expires_at = self.expires_at

        run: None | str | Unset
        if isinstance(self.run, Unset):
            run = UNSET
        else:
            run = self.run

        step: int | None | Unset
        if isinstance(self.step, Unset):
            step = UNSET
        else:
            step = self.step

        obs_start: None | str | Unset
        if isinstance(self.obs_start, Unset):
            obs_start = UNSET
        elif isinstance(self.obs_start, datetime.datetime):
            obs_start = self.obs_start.isoformat()
        else:
            obs_start = self.obs_start

        obs_end: None | str | Unset
        if isinstance(self.obs_end, Unset):
            obs_end = UNSET
        elif isinstance(self.obs_end, datetime.datetime):
            obs_end = self.obs_end.isoformat()
        else:
            obs_end = self.obs_end

        tiles2x = self.tiles2x

        image = self.image

        bounds: list[float] | None | Unset
        if isinstance(self.bounds, Unset):
            bounds = UNSET
        elif isinstance(self.bounds, list):
            bounds = self.bounds

        else:
            bounds = self.bounds

        stats: dict[str, Any] | Unset = UNSET
        if not isinstance(self.stats, Unset):
            stats = self.stats.to_dict()

        thinned = self.thinned

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "productId": product_id,
                "time": time,
                "timeKey": time_key,
                "frameKey": frame_key,
                "status": status,
                "tiles": tiles,
            }
        )
        if expires_at is not UNSET:
            field_dict["expiresAt"] = expires_at
        if run is not UNSET:
            field_dict["run"] = run
        if step is not UNSET:
            field_dict["step"] = step
        if obs_start is not UNSET:
            field_dict["obsStart"] = obs_start
        if obs_end is not UNSET:
            field_dict["obsEnd"] = obs_end
        if tiles2x is not UNSET:
            field_dict["tiles2x"] = tiles2x
        if image is not UNSET:
            field_dict["image"] = image
        if bounds is not UNSET:
            field_dict["bounds"] = bounds
        if stats is not UNSET:
            field_dict["stats"] = stats
        if thinned is not UNSET:
            field_dict["thinned"] = thinned

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.frame_stats import FrameStats

        d = dict(src_dict)
        product_id = d.pop("productId")

        time = datetime.datetime.fromisoformat(d.pop("time"))

        time_key = d.pop("timeKey")

        frame_key = d.pop("frameKey")

        status = RasterFrameStatus(d.pop("status"))

        tiles = d.pop("tiles")

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

        def _parse_run(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        run = _parse_run(d.pop("run", UNSET))

        def _parse_step(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        step = _parse_step(d.pop("step", UNSET))

        def _parse_obs_start(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                obs_start_type_0 = datetime.datetime.fromisoformat(data)

                return obs_start_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        obs_start = _parse_obs_start(d.pop("obsStart", UNSET))

        def _parse_obs_end(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                obs_end_type_0 = datetime.datetime.fromisoformat(data)

                return obs_end_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        obs_end = _parse_obs_end(d.pop("obsEnd", UNSET))

        tiles2x = d.pop("tiles2x", UNSET)

        image = d.pop("image", UNSET)

        def _parse_bounds(data: object) -> list[float] | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, list):
                    raise TypeError()
                bounds_type_0 = cast(list[float], data)

                return bounds_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(list[float] | None | Unset, data)

        bounds = _parse_bounds(d.pop("bounds", UNSET))

        _stats = d.pop("stats", UNSET)
        stats: FrameStats | Unset
        if isinstance(_stats, Unset):
            stats = UNSET
        else:
            stats = FrameStats.from_dict(_stats)

        thinned = d.pop("thinned", UNSET)

        raster_frame = cls(
            product_id=product_id,
            time=time,
            time_key=time_key,
            frame_key=frame_key,
            status=status,
            tiles=tiles,
            expires_at=expires_at,
            run=run,
            step=step,
            obs_start=obs_start,
            obs_end=obs_end,
            tiles2x=tiles2x,
            image=image,
            bounds=bounds,
            stats=stats,
            thinned=thinned,
        )

        raster_frame.additional_properties = d
        return raster_frame

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
