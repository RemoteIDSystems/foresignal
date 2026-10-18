from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.tile_json_encoding_type_1 import TileJsonEncodingType1
from ..models.tile_json_encoding_type_2_type_1 import TileJsonEncodingType2Type1
from ..models.tile_json_encoding_type_3_type_1 import TileJsonEncodingType3Type1
from ..models.tile_json_scheme import TileJsonScheme
from ..models.tile_json_tile_size import TileJsonTileSize
from ..models.tile_json_tilejson import TileJsonTilejson
from ..models.tile_json_tms import TileJsonTms
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.tile_json_vector_layers_item import TileJsonVectorLayersItem


T = TypeVar("T", bound="TileJson")


@_attrs_define
class TileJson:
    """TileJSON 3.0.0 document for one vector-tile layer (snake_case keys per the TileJSON spec)

    Attributes:
        tilejson (TileJsonTilejson):
        tiles (list[str]): Tile URL templates
        name (str | Unset):
        description (str | Unset):
        version (str | Unset): Foresignal version
        attribution (str | Unset): Distinct descriptor attributions joined with " · "
        scheme (TileJsonScheme | Unset):
        minzoom (int | Unset):
        maxzoom (int | Unset):
        bounds (list[float] | Unset):  Example: [-180, -85.0511, 180, 85.0511].
        vector_layers (list[TileJsonVectorLayersItem] | Unset):
        foresignalagg_below_zoom (int | None | Unset):
        foresignalproperties (list[str] | Unset): Feature property names
        tile_size (TileJsonTileSize | Unset): Raster products: 256
        foresignaltiles2x (list[str] | Unset): Raster products: the `@2x` (512 px) template
        foresignalframe_key (str | Unset): Raster products: resolved content-addressed frame key
        foresignalstyle (str | Unset): Raster products: versioned style `name@v`
        foresignalrun (None | str | Unset): Raster products: pinned forecast run
        foresignalexpires_at (datetime.datetime | None | Unset):
        tms (TileJsonTms | Unset):
        encoding (None | TileJsonEncodingType1 | TileJsonEncodingType2Type1 | TileJsonEncodingType3Type1 | Unset): Data
            tiles only
        red_factor (float | None | Unset):
        green_factor (float | None | Unset):
        blue_factor (float | None | Unset):
        base_shift (float | None | Unset):
        units (None | str | Unset):
        nodata (float | None | Unset):
    """

    tilejson: TileJsonTilejson
    tiles: list[str]
    name: str | Unset = UNSET
    description: str | Unset = UNSET
    version: str | Unset = UNSET
    attribution: str | Unset = UNSET
    scheme: TileJsonScheme | Unset = UNSET
    minzoom: int | Unset = UNSET
    maxzoom: int | Unset = UNSET
    bounds: list[float] | Unset = UNSET
    vector_layers: list[TileJsonVectorLayersItem] | Unset = UNSET
    foresignalagg_below_zoom: int | None | Unset = UNSET
    foresignalproperties: list[str] | Unset = UNSET
    tile_size: TileJsonTileSize | Unset = UNSET
    foresignaltiles2x: list[str] | Unset = UNSET
    foresignalframe_key: str | Unset = UNSET
    foresignalstyle: str | Unset = UNSET
    foresignalrun: None | str | Unset = UNSET
    foresignalexpires_at: datetime.datetime | None | Unset = UNSET
    tms: TileJsonTms | Unset = UNSET
    encoding: (
        None
        | TileJsonEncodingType1
        | TileJsonEncodingType2Type1
        | TileJsonEncodingType3Type1
        | Unset
    ) = UNSET
    red_factor: float | None | Unset = UNSET
    green_factor: float | None | Unset = UNSET
    blue_factor: float | None | Unset = UNSET
    base_shift: float | None | Unset = UNSET
    units: None | str | Unset = UNSET
    nodata: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        tilejson = self.tilejson.value

        tiles = self.tiles

        name = self.name

        description = self.description

        version = self.version

        attribution = self.attribution

        scheme: str | Unset = UNSET
        if not isinstance(self.scheme, Unset):
            scheme = self.scheme.value

        minzoom = self.minzoom

        maxzoom = self.maxzoom

        bounds: list[float] | Unset = UNSET
        if not isinstance(self.bounds, Unset):
            bounds = self.bounds

        vector_layers: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.vector_layers, Unset):
            vector_layers = []
            for vector_layers_item_data in self.vector_layers:
                vector_layers_item = vector_layers_item_data.to_dict()
                vector_layers.append(vector_layers_item)

        foresignalagg_below_zoom: int | None | Unset
        if isinstance(self.foresignalagg_below_zoom, Unset):
            foresignalagg_below_zoom = UNSET
        else:
            foresignalagg_below_zoom = self.foresignalagg_below_zoom

        foresignalproperties: list[str] | Unset = UNSET
        if not isinstance(self.foresignalproperties, Unset):
            foresignalproperties = self.foresignalproperties

        tile_size: int | Unset = UNSET
        if not isinstance(self.tile_size, Unset):
            tile_size = self.tile_size.value

        foresignaltiles2x: list[str] | Unset = UNSET
        if not isinstance(self.foresignaltiles2x, Unset):
            foresignaltiles2x = self.foresignaltiles2x

        foresignalframe_key = self.foresignalframe_key

        foresignalstyle = self.foresignalstyle

        foresignalrun: None | str | Unset
        if isinstance(self.foresignalrun, Unset):
            foresignalrun = UNSET
        else:
            foresignalrun = self.foresignalrun

        foresignalexpires_at: None | str | Unset
        if isinstance(self.foresignalexpires_at, Unset):
            foresignalexpires_at = UNSET
        elif isinstance(self.foresignalexpires_at, datetime.datetime):
            foresignalexpires_at = self.foresignalexpires_at.isoformat()
        else:
            foresignalexpires_at = self.foresignalexpires_at

        tms: str | Unset = UNSET
        if not isinstance(self.tms, Unset):
            tms = self.tms.value

        encoding: None | str | Unset
        if isinstance(self.encoding, Unset):
            encoding = UNSET
        elif (
            isinstance(self.encoding, TileJsonEncodingType1)
            or isinstance(self.encoding, TileJsonEncodingType2Type1)
            or isinstance(self.encoding, TileJsonEncodingType3Type1)
        ):
            encoding = self.encoding.value
        else:
            encoding = self.encoding

        red_factor: float | None | Unset
        if isinstance(self.red_factor, Unset):
            red_factor = UNSET
        else:
            red_factor = self.red_factor

        green_factor: float | None | Unset
        if isinstance(self.green_factor, Unset):
            green_factor = UNSET
        else:
            green_factor = self.green_factor

        blue_factor: float | None | Unset
        if isinstance(self.blue_factor, Unset):
            blue_factor = UNSET
        else:
            blue_factor = self.blue_factor

        base_shift: float | None | Unset
        if isinstance(self.base_shift, Unset):
            base_shift = UNSET
        else:
            base_shift = self.base_shift

        units: None | str | Unset
        if isinstance(self.units, Unset):
            units = UNSET
        else:
            units = self.units

        nodata: float | None | Unset
        if isinstance(self.nodata, Unset):
            nodata = UNSET
        else:
            nodata = self.nodata

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "tilejson": tilejson,
                "tiles": tiles,
            }
        )
        if name is not UNSET:
            field_dict["name"] = name
        if description is not UNSET:
            field_dict["description"] = description
        if version is not UNSET:
            field_dict["version"] = version
        if attribution is not UNSET:
            field_dict["attribution"] = attribution
        if scheme is not UNSET:
            field_dict["scheme"] = scheme
        if minzoom is not UNSET:
            field_dict["minzoom"] = minzoom
        if maxzoom is not UNSET:
            field_dict["maxzoom"] = maxzoom
        if bounds is not UNSET:
            field_dict["bounds"] = bounds
        if vector_layers is not UNSET:
            field_dict["vector_layers"] = vector_layers
        if foresignalagg_below_zoom is not UNSET:
            field_dict["foresignal:aggBelowZoom"] = foresignalagg_below_zoom
        if foresignalproperties is not UNSET:
            field_dict["foresignal:properties"] = foresignalproperties
        if tile_size is not UNSET:
            field_dict["tileSize"] = tile_size
        if foresignaltiles2x is not UNSET:
            field_dict["foresignal:tiles2x"] = foresignaltiles2x
        if foresignalframe_key is not UNSET:
            field_dict["foresignal:frameKey"] = foresignalframe_key
        if foresignalstyle is not UNSET:
            field_dict["foresignal:style"] = foresignalstyle
        if foresignalrun is not UNSET:
            field_dict["foresignal:run"] = foresignalrun
        if foresignalexpires_at is not UNSET:
            field_dict["foresignal:expiresAt"] = foresignalexpires_at
        if tms is not UNSET:
            field_dict["tms"] = tms
        if encoding is not UNSET:
            field_dict["encoding"] = encoding
        if red_factor is not UNSET:
            field_dict["redFactor"] = red_factor
        if green_factor is not UNSET:
            field_dict["greenFactor"] = green_factor
        if blue_factor is not UNSET:
            field_dict["blueFactor"] = blue_factor
        if base_shift is not UNSET:
            field_dict["baseShift"] = base_shift
        if units is not UNSET:
            field_dict["units"] = units
        if nodata is not UNSET:
            field_dict["nodata"] = nodata

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.tile_json_vector_layers_item import TileJsonVectorLayersItem

        d = dict(src_dict)
        tilejson = TileJsonTilejson(d.pop("tilejson"))

        tiles = cast(list[str], d.pop("tiles"))

        name = d.pop("name", UNSET)

        description = d.pop("description", UNSET)

        version = d.pop("version", UNSET)

        attribution = d.pop("attribution", UNSET)

        _scheme = d.pop("scheme", UNSET)
        scheme: TileJsonScheme | Unset
        if isinstance(_scheme, Unset):
            scheme = UNSET
        else:
            scheme = TileJsonScheme(_scheme)

        minzoom = d.pop("minzoom", UNSET)

        maxzoom = d.pop("maxzoom", UNSET)

        bounds = cast(list[float], d.pop("bounds", UNSET))

        _vector_layers = d.pop("vector_layers", UNSET)
        vector_layers: list[TileJsonVectorLayersItem] | Unset = UNSET
        if _vector_layers is not UNSET:
            vector_layers = []
            for vector_layers_item_data in _vector_layers:
                vector_layers_item = TileJsonVectorLayersItem.from_dict(
                    vector_layers_item_data
                )

                vector_layers.append(vector_layers_item)

        def _parse_foresignalagg_below_zoom(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        foresignalagg_below_zoom = _parse_foresignalagg_below_zoom(
            d.pop("foresignal:aggBelowZoom", UNSET)
        )

        foresignalproperties = cast(list[str], d.pop("foresignal:properties", UNSET))

        _tile_size = d.pop("tileSize", UNSET)
        tile_size: TileJsonTileSize | Unset
        if isinstance(_tile_size, Unset):
            tile_size = UNSET
        else:
            tile_size = TileJsonTileSize(_tile_size)

        foresignaltiles2x = cast(list[str], d.pop("foresignal:tiles2x", UNSET))

        foresignalframe_key = d.pop("foresignal:frameKey", UNSET)

        foresignalstyle = d.pop("foresignal:style", UNSET)

        def _parse_foresignalrun(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        foresignalrun = _parse_foresignalrun(d.pop("foresignal:run", UNSET))

        def _parse_foresignalexpires_at(
            data: object,
        ) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                foresignalexpires_at_type_0 = datetime.datetime.fromisoformat(data)

                return foresignalexpires_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        foresignalexpires_at = _parse_foresignalexpires_at(
            d.pop("foresignal:expiresAt", UNSET)
        )

        _tms = d.pop("tms", UNSET)
        tms: TileJsonTms | Unset
        if isinstance(_tms, Unset):
            tms = UNSET
        else:
            tms = TileJsonTms(_tms)

        def _parse_encoding(
            data: object,
        ) -> (
            None
            | TileJsonEncodingType1
            | TileJsonEncodingType2Type1
            | TileJsonEncodingType3Type1
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                encoding_type_1 = TileJsonEncodingType1(data)

                return encoding_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                encoding_type_2_type_1 = TileJsonEncodingType2Type1(data)

                return encoding_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                encoding_type_3_type_1 = TileJsonEncodingType3Type1(data)

                return encoding_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | TileJsonEncodingType1
                | TileJsonEncodingType2Type1
                | TileJsonEncodingType3Type1
                | Unset,
                data,
            )

        encoding = _parse_encoding(d.pop("encoding", UNSET))

        def _parse_red_factor(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        red_factor = _parse_red_factor(d.pop("redFactor", UNSET))

        def _parse_green_factor(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        green_factor = _parse_green_factor(d.pop("greenFactor", UNSET))

        def _parse_blue_factor(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        blue_factor = _parse_blue_factor(d.pop("blueFactor", UNSET))

        def _parse_base_shift(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        base_shift = _parse_base_shift(d.pop("baseShift", UNSET))

        def _parse_units(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        units = _parse_units(d.pop("units", UNSET))

        def _parse_nodata(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        nodata = _parse_nodata(d.pop("nodata", UNSET))

        tile_json = cls(
            tilejson=tilejson,
            tiles=tiles,
            name=name,
            description=description,
            version=version,
            attribution=attribution,
            scheme=scheme,
            minzoom=minzoom,
            maxzoom=maxzoom,
            bounds=bounds,
            vector_layers=vector_layers,
            foresignalagg_below_zoom=foresignalagg_below_zoom,
            foresignalproperties=foresignalproperties,
            tile_size=tile_size,
            foresignaltiles2x=foresignaltiles2x,
            foresignalframe_key=foresignalframe_key,
            foresignalstyle=foresignalstyle,
            foresignalrun=foresignalrun,
            foresignalexpires_at=foresignalexpires_at,
            tms=tms,
            encoding=encoding,
            red_factor=red_factor,
            green_factor=green_factor,
            blue_factor=blue_factor,
            base_shift=base_shift,
            units=units,
            nodata=nodata,
        )

        tile_json.additional_properties = d
        return tile_json

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
