from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.field_encodings_i16_type_0 import FieldEncodingsI16Type0
    from ..models.field_encodings_png_tiles_type_0 import FieldEncodingsPngTilesType0
    from ..models.field_encodings_png_uv_type_0 import FieldEncodingsPngUvType0
    from ..models.field_encodings_velocity_json_type_0 import (
        FieldEncodingsVelocityJsonType0,
    )


T = TypeVar("T", bound="FieldEncodings")


@_attrs_define
class FieldEncodings:
    """Alternative encodings of a wind field listed in the field manifest

    Attributes:
        velocity_json (FieldEncodingsVelocityJsonType0 | None | Unset):
        png_uv (FieldEncodingsPngUvType0 | None | Unset): 8-bit RGB PNG: R = u, G = v scaled linearly; B = 0
        i16 (FieldEncodingsI16Type0 | None | Unset): 108: planar little-endian int16 u, v
        png_tiles (FieldEncodingsPngTilesType0 | None | Unset): 108: tiled png-uv fields
        tilejson (None | str | Unset): TileJSON-style link
    """

    velocity_json: FieldEncodingsVelocityJsonType0 | None | Unset = UNSET
    png_uv: FieldEncodingsPngUvType0 | None | Unset = UNSET
    i16: FieldEncodingsI16Type0 | None | Unset = UNSET
    png_tiles: FieldEncodingsPngTilesType0 | None | Unset = UNSET
    tilejson: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.field_encodings_i16_type_0 import FieldEncodingsI16Type0
        from ..models.field_encodings_png_tiles_type_0 import (
            FieldEncodingsPngTilesType0,
        )
        from ..models.field_encodings_png_uv_type_0 import FieldEncodingsPngUvType0
        from ..models.field_encodings_velocity_json_type_0 import (
            FieldEncodingsVelocityJsonType0,
        )

        velocity_json: dict[str, Any] | None | Unset
        if isinstance(self.velocity_json, Unset):
            velocity_json = UNSET
        elif isinstance(self.velocity_json, FieldEncodingsVelocityJsonType0):
            velocity_json = self.velocity_json.to_dict()
        else:
            velocity_json = self.velocity_json

        png_uv: dict[str, Any] | None | Unset
        if isinstance(self.png_uv, Unset):
            png_uv = UNSET
        elif isinstance(self.png_uv, FieldEncodingsPngUvType0):
            png_uv = self.png_uv.to_dict()
        else:
            png_uv = self.png_uv

        i16: dict[str, Any] | None | Unset
        if isinstance(self.i16, Unset):
            i16 = UNSET
        elif isinstance(self.i16, FieldEncodingsI16Type0):
            i16 = self.i16.to_dict()
        else:
            i16 = self.i16

        png_tiles: dict[str, Any] | None | Unset
        if isinstance(self.png_tiles, Unset):
            png_tiles = UNSET
        elif isinstance(self.png_tiles, FieldEncodingsPngTilesType0):
            png_tiles = self.png_tiles.to_dict()
        else:
            png_tiles = self.png_tiles

        tilejson: None | str | Unset
        if isinstance(self.tilejson, Unset):
            tilejson = UNSET
        else:
            tilejson = self.tilejson

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if velocity_json is not UNSET:
            field_dict["velocityJson"] = velocity_json
        if png_uv is not UNSET:
            field_dict["pngUv"] = png_uv
        if i16 is not UNSET:
            field_dict["i16"] = i16
        if png_tiles is not UNSET:
            field_dict["pngTiles"] = png_tiles
        if tilejson is not UNSET:
            field_dict["tilejson"] = tilejson

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.field_encodings_i16_type_0 import FieldEncodingsI16Type0
        from ..models.field_encodings_png_tiles_type_0 import (
            FieldEncodingsPngTilesType0,
        )
        from ..models.field_encodings_png_uv_type_0 import FieldEncodingsPngUvType0
        from ..models.field_encodings_velocity_json_type_0 import (
            FieldEncodingsVelocityJsonType0,
        )

        d = dict(src_dict)

        def _parse_velocity_json(
            data: object,
        ) -> FieldEncodingsVelocityJsonType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                velocity_json_type_0 = FieldEncodingsVelocityJsonType0.from_dict(data)

                return velocity_json_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(FieldEncodingsVelocityJsonType0 | None | Unset, data)

        velocity_json = _parse_velocity_json(d.pop("velocityJson", UNSET))

        def _parse_png_uv(data: object) -> FieldEncodingsPngUvType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                png_uv_type_0 = FieldEncodingsPngUvType0.from_dict(data)

                return png_uv_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(FieldEncodingsPngUvType0 | None | Unset, data)

        png_uv = _parse_png_uv(d.pop("pngUv", UNSET))

        def _parse_i16(data: object) -> FieldEncodingsI16Type0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                i16_type_0 = FieldEncodingsI16Type0.from_dict(data)

                return i16_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(FieldEncodingsI16Type0 | None | Unset, data)

        i16 = _parse_i16(d.pop("i16", UNSET))

        def _parse_png_tiles(
            data: object,
        ) -> FieldEncodingsPngTilesType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                png_tiles_type_0 = FieldEncodingsPngTilesType0.from_dict(data)

                return png_tiles_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(FieldEncodingsPngTilesType0 | None | Unset, data)

        png_tiles = _parse_png_tiles(d.pop("pngTiles", UNSET))

        def _parse_tilejson(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        tilejson = _parse_tilejson(d.pop("tilejson", UNSET))

        field_encodings = cls(
            velocity_json=velocity_json,
            png_uv=png_uv,
            i16=i16,
            png_tiles=png_tiles,
            tilejson=tilejson,
        )

        field_encodings.additional_properties = d
        return field_encodings

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
