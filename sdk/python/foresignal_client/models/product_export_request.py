from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define

from ..models.product_export_request_format import ProductExportRequestFormat
from ..models.product_export_request_tms import ProductExportRequestTms
from ..types import UNSET, Unset

T = TypeVar("T", bound="ProductExportRequest")


@_attrs_define
class ProductExportRequest:
    """Raster export: one or more frames of a product as MBTiles (one file per frame), PMTiles or COG. Caps: ≤ 50,000 tiles
    per tileset and ≤ 2 GB per file.

        Attributes:
            format_ (ProductExportRequestFormat):
            frame_key (str | Unset):
            frame_keys (list[str] | Unset):
            bbox (list[float] | Unset): [w, s, e, n]
            place_id (str | Unset):
            radius_km (float | Unset):
            min_zoom (int | Unset):
            max_zoom (int | Unset):
            style (str | Unset):
            tms (ProductExportRequestTms | Unset): mbtiles and pmtiles hold XYZ WebMercatorQuad tiles only (WorldCRS84Quad
                is 400 VALIDATION_FAILED). A cog follows the set: WebMercatorQuad writes EPSG:3857, WorldCRS84Quad EPSG:4326.
                Default: ProductExportRequestTms.WEBMERCATORQUAD.
    """

    format_: ProductExportRequestFormat
    frame_key: str | Unset = UNSET
    frame_keys: list[str] | Unset = UNSET
    bbox: list[float] | Unset = UNSET
    place_id: str | Unset = UNSET
    radius_km: float | Unset = UNSET
    min_zoom: int | Unset = UNSET
    max_zoom: int | Unset = UNSET
    style: str | Unset = UNSET
    tms: ProductExportRequestTms | Unset = ProductExportRequestTms.WEBMERCATORQUAD

    def to_dict(self) -> dict[str, Any]:
        format_ = self.format_.value

        frame_key = self.frame_key

        frame_keys: list[str] | Unset = UNSET
        if not isinstance(self.frame_keys, Unset):
            frame_keys = self.frame_keys

        bbox: list[float] | Unset = UNSET
        if not isinstance(self.bbox, Unset):
            bbox = self.bbox

        place_id = self.place_id

        radius_km = self.radius_km

        min_zoom = self.min_zoom

        max_zoom = self.max_zoom

        style = self.style

        tms: str | Unset = UNSET
        if not isinstance(self.tms, Unset):
            tms = self.tms.value

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "format": format_,
            }
        )
        if frame_key is not UNSET:
            field_dict["frameKey"] = frame_key
        if frame_keys is not UNSET:
            field_dict["frameKeys"] = frame_keys
        if bbox is not UNSET:
            field_dict["bbox"] = bbox
        if place_id is not UNSET:
            field_dict["placeId"] = place_id
        if radius_km is not UNSET:
            field_dict["radiusKm"] = radius_km
        if min_zoom is not UNSET:
            field_dict["minZoom"] = min_zoom
        if max_zoom is not UNSET:
            field_dict["maxZoom"] = max_zoom
        if style is not UNSET:
            field_dict["style"] = style
        if tms is not UNSET:
            field_dict["tms"] = tms

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        format_ = ProductExportRequestFormat(d.pop("format"))

        frame_key = d.pop("frameKey", UNSET)

        frame_keys = cast(list[str], d.pop("frameKeys", UNSET))

        bbox = cast(list[float], d.pop("bbox", UNSET))

        place_id = d.pop("placeId", UNSET)

        radius_km = d.pop("radiusKm", UNSET)

        min_zoom = d.pop("minZoom", UNSET)

        max_zoom = d.pop("maxZoom", UNSET)

        style = d.pop("style", UNSET)

        _tms = d.pop("tms", UNSET)
        tms: ProductExportRequestTms | Unset
        if isinstance(_tms, Unset):
            tms = UNSET
        else:
            tms = ProductExportRequestTms(_tms)

        product_export_request = cls(
            format_=format_,
            frame_key=frame_key,
            frame_keys=frame_keys,
            bbox=bbox,
            place_id=place_id,
            radius_km=radius_km,
            min_zoom=min_zoom,
            max_zoom=max_zoom,
            style=style,
            tms=tms,
        )

        return product_export_request
