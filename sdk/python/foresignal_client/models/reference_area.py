from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.reference_area_kind import ReferenceAreaKind
from ..types import UNSET, Unset

T = TypeVar("T", bound="ReferenceArea")


@_attrs_define
class ReferenceArea:
    """A named area found by searchReferenceAreas (161): a country, an admin-1 region or a populated place

    Attributes:
        kind (ReferenceAreaKind): country and admin1 are geofenceRef codes; a place is a point
        name (str):
        label (str): The name with its admin-1 region and country, for display
        lon (float): A point inside the area (a country's label point, an admin-1 region's point on surface, a place's
            location)
        lat (float):
        code (None | str | Unset): The geofenceRef code: a country's ISO2 code or an admin-1 code; null for a place
        country_iso_2 (None | str | Unset):
        country_name (None | str | Unset):
        admin1 (None | str | Unset): The admin-1 code of an admin-1 region, or of the region a place lies in
        admin_1_name (None | str | Unset):
        place_id (int | None | Unset): A place's GeoNames id
        population (int | None | Unset): A place's population, when known
    """

    kind: ReferenceAreaKind
    name: str
    label: str
    lon: float
    lat: float
    code: None | str | Unset = UNSET
    country_iso_2: None | str | Unset = UNSET
    country_name: None | str | Unset = UNSET
    admin1: None | str | Unset = UNSET
    admin_1_name: None | str | Unset = UNSET
    place_id: int | None | Unset = UNSET
    population: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        kind = self.kind.value

        name = self.name

        label = self.label

        lon = self.lon

        lat = self.lat

        code: None | str | Unset
        if isinstance(self.code, Unset):
            code = UNSET
        else:
            code = self.code

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

        admin1: None | str | Unset
        if isinstance(self.admin1, Unset):
            admin1 = UNSET
        else:
            admin1 = self.admin1

        admin_1_name: None | str | Unset
        if isinstance(self.admin_1_name, Unset):
            admin_1_name = UNSET
        else:
            admin_1_name = self.admin_1_name

        place_id: int | None | Unset
        if isinstance(self.place_id, Unset):
            place_id = UNSET
        else:
            place_id = self.place_id

        population: int | None | Unset
        if isinstance(self.population, Unset):
            population = UNSET
        else:
            population = self.population

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "kind": kind,
                "name": name,
                "label": label,
                "lon": lon,
                "lat": lat,
            }
        )
        if code is not UNSET:
            field_dict["code"] = code
        if country_iso_2 is not UNSET:
            field_dict["countryIso2"] = country_iso_2
        if country_name is not UNSET:
            field_dict["countryName"] = country_name
        if admin1 is not UNSET:
            field_dict["admin1"] = admin1
        if admin_1_name is not UNSET:
            field_dict["admin1Name"] = admin_1_name
        if place_id is not UNSET:
            field_dict["placeId"] = place_id
        if population is not UNSET:
            field_dict["population"] = population

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        kind = ReferenceAreaKind(d.pop("kind"))

        name = d.pop("name")

        label = d.pop("label")

        lon = d.pop("lon")

        lat = d.pop("lat")

        def _parse_code(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        code = _parse_code(d.pop("code", UNSET))

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

        def _parse_admin1(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        admin1 = _parse_admin1(d.pop("admin1", UNSET))

        def _parse_admin_1_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        admin_1_name = _parse_admin_1_name(d.pop("admin1Name", UNSET))

        def _parse_place_id(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        place_id = _parse_place_id(d.pop("placeId", UNSET))

        def _parse_population(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        population = _parse_population(d.pop("population", UNSET))

        reference_area = cls(
            kind=kind,
            name=name,
            label=label,
            lon=lon,
            lat=lat,
            code=code,
            country_iso_2=country_iso_2,
            country_name=country_name,
            admin1=admin1,
            admin_1_name=admin_1_name,
            place_id=place_id,
            population=population,
        )

        reference_area.additional_properties = d
        return reference_area

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
