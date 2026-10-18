from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry


T = TypeVar("T", bound="ResolverArea")


@_attrs_define
class ResolverArea:
    """Area for a resolver: one of bbox, polygon, place or regions

    Attributes:
        bbox (list[float] | Unset):
        polygon (Geometry | Unset): GeoJSON geometry (RFC 7946), WGS84
        place_id (str | Unset): 036 place id
        region_codes (list[str] | Unset):
    """

    bbox: list[float] | Unset = UNSET
    polygon: Geometry | Unset = UNSET
    place_id: str | Unset = UNSET
    region_codes: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        bbox: list[float] | Unset = UNSET
        if not isinstance(self.bbox, Unset):
            bbox = self.bbox

        polygon: dict[str, Any] | Unset = UNSET
        if not isinstance(self.polygon, Unset):
            polygon = self.polygon.to_dict()

        place_id = self.place_id

        region_codes: list[str] | Unset = UNSET
        if not isinstance(self.region_codes, Unset):
            region_codes = self.region_codes

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if bbox is not UNSET:
            field_dict["bbox"] = bbox
        if polygon is not UNSET:
            field_dict["polygon"] = polygon
        if place_id is not UNSET:
            field_dict["placeId"] = place_id
        if region_codes is not UNSET:
            field_dict["regionCodes"] = region_codes

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry

        d = dict(src_dict)
        bbox = cast(list[float], d.pop("bbox", UNSET))

        _polygon = d.pop("polygon", UNSET)
        polygon: Geometry | Unset
        if isinstance(_polygon, Unset):
            polygon = UNSET
        else:
            polygon = Geometry.from_dict(_polygon)

        place_id = d.pop("placeId", UNSET)

        region_codes = cast(list[str], d.pop("regionCodes", UNSET))

        resolver_area = cls(
            bbox=bbox,
            polygon=polygon,
            place_id=place_id,
            region_codes=region_codes,
        )

        resolver_area.additional_properties = d
        return resolver_area

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
