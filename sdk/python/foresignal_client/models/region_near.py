from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.region_near_compass_16 import RegionNearCompass16
from ..types import UNSET, Unset

T = TypeVar("T", bound="RegionNear")


@_attrs_define
class RegionNear:
    """Nearest gazetteer place (GeoNames, CC BY 4.0) (040)

    Attributes:
        geonameid (int):
        name (str):
        distance_km (float):
        compass16 (RegionNearCompass16):
        iso2 (None | str | Unset):
        admin1 (None | str | Unset):
        bearing_deg (float | Unset):
        population (int | None | Unset):
        text (str | Unset):  Example: 12 km NNE of Austin, TX.
    """

    geonameid: int
    name: str
    distance_km: float
    compass16: RegionNearCompass16
    iso2: None | str | Unset = UNSET
    admin1: None | str | Unset = UNSET
    bearing_deg: float | Unset = UNSET
    population: int | None | Unset = UNSET
    text: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        geonameid = self.geonameid

        name = self.name

        distance_km = self.distance_km

        compass16 = self.compass16.value

        iso2: None | str | Unset
        if isinstance(self.iso2, Unset):
            iso2 = UNSET
        else:
            iso2 = self.iso2

        admin1: None | str | Unset
        if isinstance(self.admin1, Unset):
            admin1 = UNSET
        else:
            admin1 = self.admin1

        bearing_deg = self.bearing_deg

        population: int | None | Unset
        if isinstance(self.population, Unset):
            population = UNSET
        else:
            population = self.population

        text = self.text

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "geonameid": geonameid,
                "name": name,
                "distanceKm": distance_km,
                "compass16": compass16,
            }
        )
        if iso2 is not UNSET:
            field_dict["iso2"] = iso2
        if admin1 is not UNSET:
            field_dict["admin1"] = admin1
        if bearing_deg is not UNSET:
            field_dict["bearingDeg"] = bearing_deg
        if population is not UNSET:
            field_dict["population"] = population
        if text is not UNSET:
            field_dict["text"] = text

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        geonameid = d.pop("geonameid")

        name = d.pop("name")

        distance_km = d.pop("distanceKm")

        compass16 = RegionNearCompass16(d.pop("compass16"))

        def _parse_iso2(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        iso2 = _parse_iso2(d.pop("iso2", UNSET))

        def _parse_admin1(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        admin1 = _parse_admin1(d.pop("admin1", UNSET))

        bearing_deg = d.pop("bearingDeg", UNSET)

        def _parse_population(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        population = _parse_population(d.pop("population", UNSET))

        text = d.pop("text", UNSET)

        region_near = cls(
            geonameid=geonameid,
            name=name,
            distance_km=distance_km,
            compass16=compass16,
            iso2=iso2,
            admin1=admin1,
            bearing_deg=bearing_deg,
            population=population,
            text=text,
        )

        region_near.additional_properties = d
        return region_near

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
