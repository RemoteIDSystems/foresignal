from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="CountryRef")


@_attrs_define
class CountryRef:
    """One row of the bundled country table (040)

    Attributes:
        iso2 (str):
        iso3 (str):
        name (str):
        label_lon (float):
        label_lat (float):
        iso_numeric (None | str | Unset):
        fips (None | str | Unset):
        name_long (None | str | Unset):
        continent (None | str | Unset):
        aliases (list[str] | Unset):
    """

    iso2: str
    iso3: str
    name: str
    label_lon: float
    label_lat: float
    iso_numeric: None | str | Unset = UNSET
    fips: None | str | Unset = UNSET
    name_long: None | str | Unset = UNSET
    continent: None | str | Unset = UNSET
    aliases: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        iso2 = self.iso2

        iso3 = self.iso3

        name = self.name

        label_lon = self.label_lon

        label_lat = self.label_lat

        iso_numeric: None | str | Unset
        if isinstance(self.iso_numeric, Unset):
            iso_numeric = UNSET
        else:
            iso_numeric = self.iso_numeric

        fips: None | str | Unset
        if isinstance(self.fips, Unset):
            fips = UNSET
        else:
            fips = self.fips

        name_long: None | str | Unset
        if isinstance(self.name_long, Unset):
            name_long = UNSET
        else:
            name_long = self.name_long

        continent: None | str | Unset
        if isinstance(self.continent, Unset):
            continent = UNSET
        else:
            continent = self.continent

        aliases: list[str] | Unset = UNSET
        if not isinstance(self.aliases, Unset):
            aliases = self.aliases

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "iso2": iso2,
                "iso3": iso3,
                "name": name,
                "labelLon": label_lon,
                "labelLat": label_lat,
            }
        )
        if iso_numeric is not UNSET:
            field_dict["isoNumeric"] = iso_numeric
        if fips is not UNSET:
            field_dict["fips"] = fips
        if name_long is not UNSET:
            field_dict["nameLong"] = name_long
        if continent is not UNSET:
            field_dict["continent"] = continent
        if aliases is not UNSET:
            field_dict["aliases"] = aliases

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        iso2 = d.pop("iso2")

        iso3 = d.pop("iso3")

        name = d.pop("name")

        label_lon = d.pop("labelLon")

        label_lat = d.pop("labelLat")

        def _parse_iso_numeric(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        iso_numeric = _parse_iso_numeric(d.pop("isoNumeric", UNSET))

        def _parse_fips(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        fips = _parse_fips(d.pop("fips", UNSET))

        def _parse_name_long(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        name_long = _parse_name_long(d.pop("nameLong", UNSET))

        def _parse_continent(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        continent = _parse_continent(d.pop("continent", UNSET))

        aliases = cast(list[str], d.pop("aliases", UNSET))

        country_ref = cls(
            iso2=iso2,
            iso3=iso3,
            name=name,
            label_lon=label_lon,
            label_lat=label_lat,
            iso_numeric=iso_numeric,
            fips=fips,
            name_long=name_long,
            continent=continent,
            aliases=aliases,
        )

        country_ref.additional_properties = d
        return country_ref

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
