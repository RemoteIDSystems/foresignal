from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="IndicatorSignalIntelEventType0")


@_attrs_define
class IndicatorSignalIntelEventType0:
    """Intelligence event type in an area

    Attributes:
        type_ (str):
        region_codes (list[str] | Unset): ISO 3166-1 alpha-2 country or ISO 3166-2 subdivision codes
        place_id (None | str | Unset):
        bbox (None | str | Unset): minLon,minLat,maxLon,maxLat
    """

    type_: str
    region_codes: list[str] | Unset = UNSET
    place_id: None | str | Unset = UNSET
    bbox: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_

        region_codes: list[str] | Unset = UNSET
        if not isinstance(self.region_codes, Unset):
            region_codes = self.region_codes

        place_id: None | str | Unset
        if isinstance(self.place_id, Unset):
            place_id = UNSET
        else:
            place_id = self.place_id

        bbox: None | str | Unset
        if isinstance(self.bbox, Unset):
            bbox = UNSET
        else:
            bbox = self.bbox

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
            }
        )
        if region_codes is not UNSET:
            field_dict["regionCodes"] = region_codes
        if place_id is not UNSET:
            field_dict["placeId"] = place_id
        if bbox is not UNSET:
            field_dict["bbox"] = bbox

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        type_ = d.pop("type")

        region_codes = cast(list[str], d.pop("regionCodes", UNSET))

        def _parse_place_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        place_id = _parse_place_id(d.pop("placeId", UNSET))

        def _parse_bbox(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        bbox = _parse_bbox(d.pop("bbox", UNSET))

        indicator_signal_intel_event_type_0 = cls(
            type_=type_,
            region_codes=region_codes,
            place_id=place_id,
            bbox=bbox,
        )

        indicator_signal_intel_event_type_0.additional_properties = d
        return indicator_signal_intel_event_type_0

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
