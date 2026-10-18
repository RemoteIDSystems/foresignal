from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.overland_batch_locations_item import OverlandBatchLocationsItem


T = TypeVar("T", bound="OverlandBatch")


@_attrs_define
class OverlandBatch:
    """Overland batch accepted by the `overland` format (039); up to 1,000 points

    Attributes:
        locations (list[OverlandBatchLocationsItem]):
        trip (Any | Unset): Ignored
        current (Any | Unset): Ignored
    """

    locations: list[OverlandBatchLocationsItem]
    trip: Any | Unset = UNSET
    current: Any | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        locations = []
        for locations_item_data in self.locations:
            locations_item = locations_item_data.to_dict()
            locations.append(locations_item)

        trip = self.trip

        current = self.current

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "locations": locations,
            }
        )
        if trip is not UNSET:
            field_dict["trip"] = trip
        if current is not UNSET:
            field_dict["current"] = current

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.overland_batch_locations_item import OverlandBatchLocationsItem

        d = dict(src_dict)
        locations = []
        _locations = d.pop("locations")
        for locations_item_data in _locations:
            locations_item = OverlandBatchLocationsItem.from_dict(locations_item_data)

            locations.append(locations_item)

        trip = d.pop("trip", UNSET)

        current = d.pop("current", UNSET)

        overland_batch = cls(
            locations=locations,
            trip=trip,
            current=current,
        )

        overland_batch.additional_properties = d
        return overland_batch

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
