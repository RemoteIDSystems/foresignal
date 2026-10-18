from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="HoldingPatternPayloadNearestAirportType0")


@_attrs_define
class HoldingPatternPayloadNearestAirportType0:
    """Nearest OurAirports airport within 50 km

    Attributes:
        ident (str):
        name (str | Unset):
        distance_km (float | Unset):
    """

    ident: str
    name: str | Unset = UNSET
    distance_km: float | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        ident = self.ident

        name = self.name

        distance_km = self.distance_km

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ident": ident,
            }
        )
        if name is not UNSET:
            field_dict["name"] = name
        if distance_km is not UNSET:
            field_dict["distanceKm"] = distance_km

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        ident = d.pop("ident")

        name = d.pop("name", UNSET)

        distance_km = d.pop("distanceKm", UNSET)

        holding_pattern_payload_nearest_airport_type_0 = cls(
            ident=ident,
            name=name,
            distance_km=distance_km,
        )

        holding_pattern_payload_nearest_airport_type_0.additional_properties = d
        return holding_pattern_payload_nearest_airport_type_0

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
