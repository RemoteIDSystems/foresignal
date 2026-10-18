from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.place_input_kind import PlaceInputKind
from ..models.place_input_shape import PlaceInputShape
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry
    from ..models.place_external_type_0 import PlaceExternalType0
    from ..models.place_input_attributes import PlaceInputAttributes


T = TypeVar("T", bound="Place")


@_attrs_define
class Place:
    """A named place: point + radius, polygon or route + buffer, with its derived area (036)

    Attributes:
        id (str):
        name (str):
        shape (PlaceInputShape):
        geometry (Geometry): GeoJSON geometry (RFC 7946), WGS84
        version (int): Bumped on every save
        created_at (datetime.datetime):
        updated_at (datetime.datetime):
        radius_km (float | Unset): point only Default: 20.0.
        buffer_m (float | Unset): route only
        timezone (None | str | Unset): IANA zone (default: server timezone)
        kind (PlaceInputKind | Unset):  Default: PlaceInputKind.USER.
        tags (list[str] | Unset):
        products (list[str] | Unset): Weather product ids for place-weather (052)
        sensitive (bool | Unset):  Default: True.
        attributes (PlaceInputAttributes | Unset):
        area (Geometry | Unset): GeoJSON geometry (RFC 7946), WGS84
        external (None | PlaceExternalType0 | Unset): Latest values or documents of external streams bound to this place
            (138)
    """

    id: str
    name: str
    shape: PlaceInputShape
    geometry: Geometry
    version: int
    created_at: datetime.datetime
    updated_at: datetime.datetime
    radius_km: float | Unset = 20.0
    buffer_m: float | Unset = UNSET
    timezone: None | str | Unset = UNSET
    kind: PlaceInputKind | Unset = PlaceInputKind.USER
    tags: list[str] | Unset = UNSET
    products: list[str] | Unset = UNSET
    sensitive: bool | Unset = True
    attributes: PlaceInputAttributes | Unset = UNSET
    area: Geometry | Unset = UNSET
    external: None | PlaceExternalType0 | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.place_external_type_0 import PlaceExternalType0

        id = self.id

        name = self.name

        shape = self.shape.value

        geometry = self.geometry.to_dict()

        version = self.version

        created_at = self.created_at.isoformat()

        updated_at = self.updated_at.isoformat()

        radius_km = self.radius_km

        buffer_m = self.buffer_m

        timezone: None | str | Unset
        if isinstance(self.timezone, Unset):
            timezone = UNSET
        else:
            timezone = self.timezone

        kind: str | Unset = UNSET
        if not isinstance(self.kind, Unset):
            kind = self.kind.value

        tags: list[str] | Unset = UNSET
        if not isinstance(self.tags, Unset):
            tags = self.tags

        products: list[str] | Unset = UNSET
        if not isinstance(self.products, Unset):
            products = self.products

        sensitive = self.sensitive

        attributes: dict[str, Any] | Unset = UNSET
        if not isinstance(self.attributes, Unset):
            attributes = self.attributes.to_dict()

        area: dict[str, Any] | Unset = UNSET
        if not isinstance(self.area, Unset):
            area = self.area.to_dict()

        external: dict[str, Any] | None | Unset
        if isinstance(self.external, Unset):
            external = UNSET
        elif isinstance(self.external, PlaceExternalType0):
            external = self.external.to_dict()
        else:
            external = self.external

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "name": name,
                "shape": shape,
                "geometry": geometry,
                "version": version,
                "createdAt": created_at,
                "updatedAt": updated_at,
            }
        )
        if radius_km is not UNSET:
            field_dict["radiusKm"] = radius_km
        if buffer_m is not UNSET:
            field_dict["bufferM"] = buffer_m
        if timezone is not UNSET:
            field_dict["timezone"] = timezone
        if kind is not UNSET:
            field_dict["kind"] = kind
        if tags is not UNSET:
            field_dict["tags"] = tags
        if products is not UNSET:
            field_dict["products"] = products
        if sensitive is not UNSET:
            field_dict["sensitive"] = sensitive
        if attributes is not UNSET:
            field_dict["attributes"] = attributes
        if area is not UNSET:
            field_dict["area"] = area
        if external is not UNSET:
            field_dict["external"] = external

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry
        from ..models.place_external_type_0 import PlaceExternalType0
        from ..models.place_input_attributes import PlaceInputAttributes

        d = dict(src_dict)
        id = d.pop("id")

        name = d.pop("name")

        shape = PlaceInputShape(d.pop("shape"))

        geometry = Geometry.from_dict(d.pop("geometry"))

        version = d.pop("version")

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        updated_at = datetime.datetime.fromisoformat(d.pop("updatedAt"))

        radius_km = d.pop("radiusKm", UNSET)

        buffer_m = d.pop("bufferM", UNSET)

        def _parse_timezone(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        timezone = _parse_timezone(d.pop("timezone", UNSET))

        _kind = d.pop("kind", UNSET)
        kind: PlaceInputKind | Unset
        if isinstance(_kind, Unset):
            kind = UNSET
        else:
            kind = PlaceInputKind(_kind)

        tags = cast(list[str], d.pop("tags", UNSET))

        products = cast(list[str], d.pop("products", UNSET))

        sensitive = d.pop("sensitive", UNSET)

        _attributes = d.pop("attributes", UNSET)
        attributes: PlaceInputAttributes | Unset
        if isinstance(_attributes, Unset):
            attributes = UNSET
        else:
            attributes = PlaceInputAttributes.from_dict(_attributes)

        _area = d.pop("area", UNSET)
        area: Geometry | Unset
        if isinstance(_area, Unset):
            area = UNSET
        else:
            area = Geometry.from_dict(_area)

        def _parse_external(data: object) -> None | PlaceExternalType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                external_type_0 = PlaceExternalType0.from_dict(data)

                return external_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | PlaceExternalType0 | Unset, data)

        external = _parse_external(d.pop("external", UNSET))

        place = cls(
            id=id,
            name=name,
            shape=shape,
            geometry=geometry,
            version=version,
            created_at=created_at,
            updated_at=updated_at,
            radius_km=radius_km,
            buffer_m=buffer_m,
            timezone=timezone,
            kind=kind,
            tags=tags,
            products=products,
            sensitive=sensitive,
            attributes=attributes,
            area=area,
            external=external,
        )

        place.additional_properties = d
        return place

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
