from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="SamplePosition")


@_attrs_define
class SamplePosition:
    """Live position of the sampled record (head ⋈ live)

    Attributes:
        lon (float):
        lat (float):
        observed_at (datetime.datetime):
        age_seconds (int):
    """

    lon: float
    lat: float
    observed_at: datetime.datetime
    age_seconds: int
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        lon = self.lon

        lat = self.lat

        observed_at = self.observed_at.isoformat()

        age_seconds = self.age_seconds

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "lon": lon,
                "lat": lat,
                "observedAt": observed_at,
                "ageSeconds": age_seconds,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        lon = d.pop("lon")

        lat = d.pop("lat")

        observed_at = datetime.datetime.fromisoformat(d.pop("observedAt"))

        age_seconds = d.pop("ageSeconds")

        sample_position = cls(
            lon=lon,
            lat=lat,
            observed_at=observed_at,
            age_seconds=age_seconds,
        )

        sample_position.additional_properties = d
        return sample_position

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
