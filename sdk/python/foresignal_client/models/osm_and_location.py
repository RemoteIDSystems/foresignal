from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.osm_and_location_location import OsmAndLocationLocation


T = TypeVar("T", bound="OsmAndLocation")


@_attrs_define
class OsmAndLocation:
    """Traccar Client JSON body accepted by the `osmand` format (039)

    Attributes:
        device_id (str):
        location (OsmAndLocationLocation): Fix
    """

    device_id: str
    location: OsmAndLocationLocation
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        device_id = self.device_id

        location = self.location.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "device_id": device_id,
                "location": location,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.osm_and_location_location import OsmAndLocationLocation

        d = dict(src_dict)
        device_id = d.pop("device_id")

        location = OsmAndLocationLocation.from_dict(d.pop("location"))

        osm_and_location = cls(
            device_id=device_id,
            location=location,
        )

        osm_and_location.additional_properties = d
        return osm_and_location

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
