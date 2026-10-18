from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.external_catalogue_entry_format import ExternalCatalogueEntryFormat
from ..models.external_catalogue_entry_protocol import ExternalCatalogueEntryProtocol
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.attribution import Attribution
    from ..models.external_catalogue_entry_time_dimension_type_0 import (
        ExternalCatalogueEntryTimeDimensionType0,
    )


T = TypeVar("T", bound="ExternalCatalogueEntry")


@_attrs_define
class ExternalCatalogueEntry:
    """Client-direct external layer (GIBS, nowCOAST); listed by `/v1/products?kind=external`, never fetched by Foresignal

    Attributes:
        external_template (str): Tile URL template clients fetch directly (Foresignal never proxies it) Example:
            https://gibs.earthdata.nasa.gov/wmts/epsg3857/best/GOES-
            East_ABI_GeoColor/default/{time}/GoogleMapsCompatible_Level7/{z}/{y}/{x}.jpg.
        tiling_scheme (str):  Example: GoogleMapsCompatible_Level7.
        tile_size (int):  Example: 256.
        max_native_zoom (int):
        bounds (list[float]): [west, south, east, north] in WGS84 degrees
        attribution (Attribution): Structured attribution clients must render for a product or derived output (Weather-
            Imagery-Design §6.10)
        provider (str | Unset): Upstream provider (gibs, nowcoast) Example: gibs.
        layer (str | Unset):  Example: GOES-East_ABI_GeoColor.
        protocol (ExternalCatalogueEntryProtocol | Unset):
        format_ (ExternalCatalogueEntryFormat | Unset):
        time_dimension (ExternalCatalogueEntryTimeDimensionType0 | None | Unset):
        cors (str | Unset): Upstream `Access-Control-Allow-Origin` (checked 2026-10) Example: *.
    """

    external_template: str
    tiling_scheme: str
    tile_size: int
    max_native_zoom: int
    bounds: list[float]
    attribution: Attribution
    provider: str | Unset = UNSET
    layer: str | Unset = UNSET
    protocol: ExternalCatalogueEntryProtocol | Unset = UNSET
    format_: ExternalCatalogueEntryFormat | Unset = UNSET
    time_dimension: ExternalCatalogueEntryTimeDimensionType0 | None | Unset = UNSET
    cors: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.external_catalogue_entry_time_dimension_type_0 import (
            ExternalCatalogueEntryTimeDimensionType0,
        )

        external_template = self.external_template

        tiling_scheme = self.tiling_scheme

        tile_size = self.tile_size

        max_native_zoom = self.max_native_zoom

        bounds = self.bounds

        attribution = self.attribution.to_dict()

        provider = self.provider

        layer = self.layer

        protocol: str | Unset = UNSET
        if not isinstance(self.protocol, Unset):
            protocol = self.protocol.value

        format_: str | Unset = UNSET
        if not isinstance(self.format_, Unset):
            format_ = self.format_.value

        time_dimension: dict[str, Any] | None | Unset
        if isinstance(self.time_dimension, Unset):
            time_dimension = UNSET
        elif isinstance(self.time_dimension, ExternalCatalogueEntryTimeDimensionType0):
            time_dimension = self.time_dimension.to_dict()
        else:
            time_dimension = self.time_dimension

        cors = self.cors

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "externalTemplate": external_template,
                "tilingScheme": tiling_scheme,
                "tileSize": tile_size,
                "maxNativeZoom": max_native_zoom,
                "bounds": bounds,
                "attribution": attribution,
            }
        )
        if provider is not UNSET:
            field_dict["provider"] = provider
        if layer is not UNSET:
            field_dict["layer"] = layer
        if protocol is not UNSET:
            field_dict["protocol"] = protocol
        if format_ is not UNSET:
            field_dict["format"] = format_
        if time_dimension is not UNSET:
            field_dict["timeDimension"] = time_dimension
        if cors is not UNSET:
            field_dict["cors"] = cors

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.attribution import Attribution
        from ..models.external_catalogue_entry_time_dimension_type_0 import (
            ExternalCatalogueEntryTimeDimensionType0,
        )

        d = dict(src_dict)
        external_template = d.pop("externalTemplate")

        tiling_scheme = d.pop("tilingScheme")

        tile_size = d.pop("tileSize")

        max_native_zoom = d.pop("maxNativeZoom")

        bounds = cast(list[float], d.pop("bounds"))

        attribution = Attribution.from_dict(d.pop("attribution"))

        provider = d.pop("provider", UNSET)

        layer = d.pop("layer", UNSET)

        _protocol = d.pop("protocol", UNSET)
        protocol: ExternalCatalogueEntryProtocol | Unset
        if isinstance(_protocol, Unset):
            protocol = UNSET
        else:
            protocol = ExternalCatalogueEntryProtocol(_protocol)

        _format_ = d.pop("format", UNSET)
        format_: ExternalCatalogueEntryFormat | Unset
        if isinstance(_format_, Unset):
            format_ = UNSET
        else:
            format_ = ExternalCatalogueEntryFormat(_format_)

        def _parse_time_dimension(
            data: object,
        ) -> ExternalCatalogueEntryTimeDimensionType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                time_dimension_type_0 = (
                    ExternalCatalogueEntryTimeDimensionType0.from_dict(data)
                )

                return time_dimension_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ExternalCatalogueEntryTimeDimensionType0 | None | Unset, data)

        time_dimension = _parse_time_dimension(d.pop("timeDimension", UNSET))

        cors = d.pop("cors", UNSET)

        external_catalogue_entry = cls(
            external_template=external_template,
            tiling_scheme=tiling_scheme,
            tile_size=tile_size,
            max_native_zoom=max_native_zoom,
            bounds=bounds,
            attribution=attribution,
            provider=provider,
            layer=layer,
            protocol=protocol,
            format_=format_,
            time_dimension=time_dimension,
            cors=cors,
        )

        external_catalogue_entry.additional_properties = d
        return external_catalogue_entry

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
