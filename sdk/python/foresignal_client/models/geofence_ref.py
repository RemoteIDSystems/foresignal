from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="GeofenceRef")


@_attrs_define
class GeofenceRef:
    """Region preset used as a rule geofence (040)

    Attributes:
        country (list[str] | Unset):
        admin1 (list[str] | Unset):
        county (list[str] | Unset):
    """

    country: list[str] | Unset = UNSET
    admin1: list[str] | Unset = UNSET
    county: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        country: list[str] | Unset = UNSET
        if not isinstance(self.country, Unset):
            country = self.country

        admin1: list[str] | Unset = UNSET
        if not isinstance(self.admin1, Unset):
            admin1 = self.admin1

        county: list[str] | Unset = UNSET
        if not isinstance(self.county, Unset):
            county = self.county

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if country is not UNSET:
            field_dict["country"] = country
        if admin1 is not UNSET:
            field_dict["admin1"] = admin1
        if county is not UNSET:
            field_dict["county"] = county

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        country = cast(list[str], d.pop("country", UNSET))

        admin1 = cast(list[str], d.pop("admin1", UNSET))

        county = cast(list[str], d.pop("county", UNSET))

        geofence_ref = cls(
            country=country,
            admin1=admin1,
            county=county,
        )

        geofence_ref.additional_properties = d
        return geofence_ref

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
