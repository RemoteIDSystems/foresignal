from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.declared_observability_kinds_additional_property_area_type import (
    DeclaredObservabilityKindsAdditionalPropertyAreaType,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="DeclaredObservabilityKindsAdditionalPropertyArea")


@_attrs_define
class DeclaredObservabilityKindsAdditionalPropertyArea:
    """Where the source can observe

    Attributes:
        type_ (DeclaredObservabilityKindsAdditionalPropertyAreaType):
        bbox (list[float] | None | Unset): [minLon, minLat, maxLon, maxLat] (WGS84)
        region_codes (list[str] | Unset):
    """

    type_: DeclaredObservabilityKindsAdditionalPropertyAreaType
    bbox: list[float] | None | Unset = UNSET
    region_codes: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_.value

        bbox: list[float] | None | Unset
        if isinstance(self.bbox, Unset):
            bbox = UNSET
        elif isinstance(self.bbox, list):
            bbox = self.bbox

        else:
            bbox = self.bbox

        region_codes: list[str] | Unset = UNSET
        if not isinstance(self.region_codes, Unset):
            region_codes = self.region_codes

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
            }
        )
        if bbox is not UNSET:
            field_dict["bbox"] = bbox
        if region_codes is not UNSET:
            field_dict["regionCodes"] = region_codes

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        type_ = DeclaredObservabilityKindsAdditionalPropertyAreaType(d.pop("type"))

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

        region_codes = cast(list[str], d.pop("regionCodes", UNSET))

        declared_observability_kinds_additional_property_area = cls(
            type_=type_,
            bbox=bbox,
            region_codes=region_codes,
        )

        declared_observability_kinds_additional_property_area.additional_properties = d
        return declared_observability_kinds_additional_property_area

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
