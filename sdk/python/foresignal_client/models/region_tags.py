from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.region_near import RegionNear


T = TypeVar("T", bound="RegionTags")


@_attrs_define
class RegionTags:
    """Region context of a point or record (040); stored outside the hash

    Attributes:
        iso2 (None | str | Unset): null at sea
        admin1 (None | str | Unset): ISO 3166-2, e.g. US-TX
        admin2 (None | str | Unset): US county FIPS5
        sea (None | str | Unset):
        near (None | RegionNear | Unset):
        country_name (None | str | Unset):
        admin_1_name (None | str | Unset):
        county_name (None | str | Unset):
        attribution (None | str | Unset): GeoNames attribution when `near` is present
    """

    iso2: None | str | Unset = UNSET
    admin1: None | str | Unset = UNSET
    admin2: None | str | Unset = UNSET
    sea: None | str | Unset = UNSET
    near: None | RegionNear | Unset = UNSET
    country_name: None | str | Unset = UNSET
    admin_1_name: None | str | Unset = UNSET
    county_name: None | str | Unset = UNSET
    attribution: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.region_near import RegionNear

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

        admin2: None | str | Unset
        if isinstance(self.admin2, Unset):
            admin2 = UNSET
        else:
            admin2 = self.admin2

        sea: None | str | Unset
        if isinstance(self.sea, Unset):
            sea = UNSET
        else:
            sea = self.sea

        near: dict[str, Any] | None | Unset
        if isinstance(self.near, Unset):
            near = UNSET
        elif isinstance(self.near, RegionNear):
            near = self.near.to_dict()
        else:
            near = self.near

        country_name: None | str | Unset
        if isinstance(self.country_name, Unset):
            country_name = UNSET
        else:
            country_name = self.country_name

        admin_1_name: None | str | Unset
        if isinstance(self.admin_1_name, Unset):
            admin_1_name = UNSET
        else:
            admin_1_name = self.admin_1_name

        county_name: None | str | Unset
        if isinstance(self.county_name, Unset):
            county_name = UNSET
        else:
            county_name = self.county_name

        attribution: None | str | Unset
        if isinstance(self.attribution, Unset):
            attribution = UNSET
        else:
            attribution = self.attribution

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if iso2 is not UNSET:
            field_dict["iso2"] = iso2
        if admin1 is not UNSET:
            field_dict["admin1"] = admin1
        if admin2 is not UNSET:
            field_dict["admin2"] = admin2
        if sea is not UNSET:
            field_dict["sea"] = sea
        if near is not UNSET:
            field_dict["near"] = near
        if country_name is not UNSET:
            field_dict["countryName"] = country_name
        if admin_1_name is not UNSET:
            field_dict["admin1Name"] = admin_1_name
        if county_name is not UNSET:
            field_dict["countyName"] = county_name
        if attribution is not UNSET:
            field_dict["attribution"] = attribution

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.region_near import RegionNear

        d = dict(src_dict)

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

        def _parse_admin2(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        admin2 = _parse_admin2(d.pop("admin2", UNSET))

        def _parse_sea(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        sea = _parse_sea(d.pop("sea", UNSET))

        def _parse_near(data: object) -> None | RegionNear | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                near_type_0 = RegionNear.from_dict(data)

                return near_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | RegionNear | Unset, data)

        near = _parse_near(d.pop("near", UNSET))

        def _parse_country_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        country_name = _parse_country_name(d.pop("countryName", UNSET))

        def _parse_admin_1_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        admin_1_name = _parse_admin_1_name(d.pop("admin1Name", UNSET))

        def _parse_county_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        county_name = _parse_county_name(d.pop("countyName", UNSET))

        def _parse_attribution(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        attribution = _parse_attribution(d.pop("attribution", UNSET))

        region_tags = cls(
            iso2=iso2,
            admin1=admin1,
            admin2=admin2,
            sea=sea,
            near=near,
            country_name=country_name,
            admin_1_name=admin_1_name,
            county_name=county_name,
            attribution=attribution,
        )

        region_tags.additional_properties = d
        return region_tags

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
