from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.sensor import Sensor


T = TypeVar("T", bound="SensorDiscoveryResult")


@_attrs_define
class SensorDiscoveryResult:
    """What can observe an area in a window

    Attributes:
        from_ (datetime.datetime):
        to (datetime.datetime):
        items (list[Sensor]):
        bbox (list[float] | None | Unset):
        place_id (None | str | Unset):
        truncated (bool | Unset):
        note (str | Unset):  Default: 'Foresignal cannot task public satellites or third-party sensors'.
    """

    from_: datetime.datetime
    to: datetime.datetime
    items: list[Sensor]
    bbox: list[float] | None | Unset = UNSET
    place_id: None | str | Unset = UNSET
    truncated: bool | Unset = UNSET
    note: str | Unset = (
        "Foresignal cannot task public satellites or third-party sensors"
    )
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from_ = self.from_.isoformat()

        to = self.to.isoformat()

        items = []
        for items_item_data in self.items:
            items_item = items_item_data.to_dict()
            items.append(items_item)

        bbox: list[float] | None | Unset
        if isinstance(self.bbox, Unset):
            bbox = UNSET
        elif isinstance(self.bbox, list):
            bbox = self.bbox

        else:
            bbox = self.bbox

        place_id: None | str | Unset
        if isinstance(self.place_id, Unset):
            place_id = UNSET
        else:
            place_id = self.place_id

        truncated = self.truncated

        note = self.note

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "from": from_,
                "to": to,
                "items": items,
            }
        )
        if bbox is not UNSET:
            field_dict["bbox"] = bbox
        if place_id is not UNSET:
            field_dict["placeId"] = place_id
        if truncated is not UNSET:
            field_dict["truncated"] = truncated
        if note is not UNSET:
            field_dict["note"] = note

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.sensor import Sensor

        d = dict(src_dict)
        from_ = datetime.datetime.fromisoformat(d.pop("from"))

        to = datetime.datetime.fromisoformat(d.pop("to"))

        items = []
        _items = d.pop("items")
        for items_item_data in _items:
            items_item = Sensor.from_dict(items_item_data)

            items.append(items_item)

        def _parse_bbox(data: object) -> list[float] | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, list):
                    raise TypeError()
                bbox_type_0 = cast(list[float], data)

                return bbox_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(list[float] | None | Unset, data)

        bbox = _parse_bbox(d.pop("bbox", UNSET))

        def _parse_place_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        place_id = _parse_place_id(d.pop("placeId", UNSET))

        truncated = d.pop("truncated", UNSET)

        note = d.pop("note", UNSET)

        sensor_discovery_result = cls(
            from_=from_,
            to=to,
            items=items,
            bbox=bbox,
            place_id=place_id,
            truncated=truncated,
            note=note,
        )

        sensor_discovery_result.additional_properties = d
        return sensor_discovery_result

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
