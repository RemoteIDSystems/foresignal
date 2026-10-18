from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.normalized_record_preview_geometry_type_0_type import (
    NormalizedRecordPreviewGeometryType0Type,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="NormalizedRecordPreviewGeometryType0")


@_attrs_define
class NormalizedRecordPreviewGeometryType0:
    """GeoJSON geometry (RFC 7946), WGS84

    Attributes:
        type_ (NormalizedRecordPreviewGeometryType0Type):
        coordinates (list[Any] | Unset):
    """

    type_: NormalizedRecordPreviewGeometryType0Type
    coordinates: list[Any] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_.value

        coordinates: list[Any] | Unset = UNSET
        if not isinstance(self.coordinates, Unset):
            coordinates = self.coordinates

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
            }
        )
        if coordinates is not UNSET:
            field_dict["coordinates"] = coordinates

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        type_ = NormalizedRecordPreviewGeometryType0Type(d.pop("type"))

        coordinates = cast(list[Any], d.pop("coordinates", UNSET))

        normalized_record_preview_geometry_type_0 = cls(
            type_=type_,
            coordinates=coordinates,
        )

        normalized_record_preview_geometry_type_0.additional_properties = d
        return normalized_record_preview_geometry_type_0

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
