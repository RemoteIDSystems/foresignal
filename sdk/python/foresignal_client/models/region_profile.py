from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="RegionProfile")


@_attrs_define
class RegionProfile:
    """Reporting density of a region, used for `LOW_REPORTING_DENSITY`

    Attributes:
        region_code (str): 040 region code: ISO 3166-1 alpha-2 country, `CC-admin1`, or `ZZ` for untagged Example: YE.
        reporting_density (float): Weekly text-source event count ÷ median of similar-population countries
        density_decile (int):
        computed_at (datetime.datetime):
    """

    region_code: str
    reporting_density: float
    density_decile: int
    computed_at: datetime.datetime
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        region_code = self.region_code

        reporting_density = self.reporting_density

        density_decile = self.density_decile

        computed_at = self.computed_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "regionCode": region_code,
                "reportingDensity": reporting_density,
                "densityDecile": density_decile,
                "computedAt": computed_at,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        region_code = d.pop("regionCode")

        reporting_density = d.pop("reportingDensity")

        density_decile = d.pop("densityDecile")

        computed_at = datetime.datetime.fromisoformat(d.pop("computedAt"))

        region_profile = cls(
            region_code=region_code,
            reporting_density=reporting_density,
            density_decile=density_decile,
            computed_at=computed_at,
        )

        region_profile.additional_properties = d
        return region_profile

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
