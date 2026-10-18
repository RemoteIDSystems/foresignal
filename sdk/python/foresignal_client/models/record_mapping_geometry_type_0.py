from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.mapping_value_spec_type_0 import MappingValueSpecType0
    from ..models.mapping_value_spec_type_1 import MappingValueSpecType1
    from ..models.mapping_value_spec_type_2 import MappingValueSpecType2


T = TypeVar("T", bound="RecordMappingGeometryType0")


@_attrs_define
class RecordMappingGeometryType0:
    """
    Attributes:
        lon (MappingValueSpecType0 | MappingValueSpecType1 | MappingValueSpecType2): One mapping value: a Jayway
            JSONPath (item-relative under `select`; `$pairs.<key>` reads a flattened pair), a substitution-only template, or
            a constant (059)
        lat (MappingValueSpecType0 | MappingValueSpecType1 | MappingValueSpecType2): One mapping value: a Jayway
            JSONPath (item-relative under `select`; `$pairs.<key>` reads a flattened pair), a substitution-only template, or
            a constant (059)
        fallback (list[float] | Unset): [lon, lat] used when lon/lat (or geojson) give no geometry
    """

    lon: MappingValueSpecType0 | MappingValueSpecType1 | MappingValueSpecType2
    lat: MappingValueSpecType0 | MappingValueSpecType1 | MappingValueSpecType2
    fallback: list[float] | Unset = UNSET

    def to_dict(self) -> dict[str, Any]:
        from ..models.mapping_value_spec_type_0 import MappingValueSpecType0
        from ..models.mapping_value_spec_type_1 import MappingValueSpecType1

        lon: dict[str, Any]
        if isinstance(self.lon, MappingValueSpecType0) or isinstance(
            self.lon, MappingValueSpecType1
        ):
            lon = self.lon.to_dict()
        else:
            lon = self.lon.to_dict()

        lat: dict[str, Any]
        if isinstance(self.lat, MappingValueSpecType0) or isinstance(
            self.lat, MappingValueSpecType1
        ):
            lat = self.lat.to_dict()
        else:
            lat = self.lat.to_dict()

        fallback: list[float] | Unset = UNSET
        if not isinstance(self.fallback, Unset):
            fallback = self.fallback

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "lon": lon,
                "lat": lat,
            }
        )
        if fallback is not UNSET:
            field_dict["fallback"] = fallback

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.mapping_value_spec_type_0 import MappingValueSpecType0
        from ..models.mapping_value_spec_type_1 import MappingValueSpecType1
        from ..models.mapping_value_spec_type_2 import MappingValueSpecType2

        d = dict(src_dict)

        def _parse_lon(
            data: object,
        ) -> MappingValueSpecType0 | MappingValueSpecType1 | MappingValueSpecType2:
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_mapping_value_spec_type_0 = (
                    MappingValueSpecType0.from_dict(data)
                )

                return componentsschemas_mapping_value_spec_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_mapping_value_spec_type_1 = (
                    MappingValueSpecType1.from_dict(data)
                )

                return componentsschemas_mapping_value_spec_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            if not isinstance(data, dict):
                raise TypeError()
            componentsschemas_mapping_value_spec_type_2 = (
                MappingValueSpecType2.from_dict(data)
            )

            return componentsschemas_mapping_value_spec_type_2

        lon = _parse_lon(d.pop("lon"))

        def _parse_lat(
            data: object,
        ) -> MappingValueSpecType0 | MappingValueSpecType1 | MappingValueSpecType2:
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_mapping_value_spec_type_0 = (
                    MappingValueSpecType0.from_dict(data)
                )

                return componentsschemas_mapping_value_spec_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_mapping_value_spec_type_1 = (
                    MappingValueSpecType1.from_dict(data)
                )

                return componentsschemas_mapping_value_spec_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            if not isinstance(data, dict):
                raise TypeError()
            componentsschemas_mapping_value_spec_type_2 = (
                MappingValueSpecType2.from_dict(data)
            )

            return componentsschemas_mapping_value_spec_type_2

        lat = _parse_lat(d.pop("lat"))

        fallback = cast(list[float], d.pop("fallback", UNSET))

        record_mapping_geometry_type_0 = cls(
            lon=lon,
            lat=lat,
            fallback=fallback,
        )

        return record_mapping_geometry_type_0
