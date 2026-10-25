from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.reference_area_outline_kind import ReferenceAreaOutlineKind
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry


T = TypeVar("T", bound="ReferenceAreaOutline")


@_attrs_define
class ReferenceAreaOutline:
    """The outline of a country or admin-1 region (161), simplified for display

    Attributes:
        kind (ReferenceAreaOutlineKind):
        code (str):
        name (str):
        bbox (list[float]): [west, south, east, north]
        geometry (Geometry): GeoJSON geometry (RFC 7946), WGS84
        country_iso_2 (None | str | Unset):
        country_name (None | str | Unset):
    """

    kind: ReferenceAreaOutlineKind
    code: str
    name: str
    bbox: list[float]
    geometry: Geometry
    country_iso_2: None | str | Unset = UNSET
    country_name: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        kind = self.kind.value

        code = self.code

        name = self.name

        bbox = self.bbox

        geometry = self.geometry.to_dict()

        country_iso_2: None | str | Unset
        if isinstance(self.country_iso_2, Unset):
            country_iso_2 = UNSET
        else:
            country_iso_2 = self.country_iso_2

        country_name: None | str | Unset
        if isinstance(self.country_name, Unset):
            country_name = UNSET
        else:
            country_name = self.country_name

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "kind": kind,
                "code": code,
                "name": name,
                "bbox": bbox,
                "geometry": geometry,
            }
        )
        if country_iso_2 is not UNSET:
            field_dict["countryIso2"] = country_iso_2
        if country_name is not UNSET:
            field_dict["countryName"] = country_name

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry

        d = dict(src_dict)
        kind = ReferenceAreaOutlineKind(d.pop("kind"))

        code = d.pop("code")

        name = d.pop("name")

        bbox = cast(list[float], d.pop("bbox"))

        geometry = Geometry.from_dict(d.pop("geometry"))

        def _parse_country_iso_2(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        country_iso_2 = _parse_country_iso_2(d.pop("countryIso2", UNSET))

        def _parse_country_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        country_name = _parse_country_name(d.pop("countryName", UNSET))

        reference_area_outline = cls(
            kind=kind,
            code=code,
            name=name,
            bbox=bbox,
            geometry=geometry,
            country_iso_2=country_iso_2,
            country_name=country_name,
        )

        reference_area_outline.additional_properties = d
        return reference_area_outline

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
