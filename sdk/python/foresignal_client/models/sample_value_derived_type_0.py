from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SampleValueDerivedType0")


@_attrs_define
class SampleValueDerivedType0:
    """Speed and direction derived from u/v

    Attributes:
        speed_ms (float | Unset):
        from_deg (int | Unset):
        compass (str | Unset):  Example: WSW.
    """

    speed_ms: float | Unset = UNSET
    from_deg: int | Unset = UNSET
    compass: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        speed_ms = self.speed_ms

        from_deg = self.from_deg

        compass = self.compass

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if speed_ms is not UNSET:
            field_dict["speedMs"] = speed_ms
        if from_deg is not UNSET:
            field_dict["fromDeg"] = from_deg
        if compass is not UNSET:
            field_dict["compass"] = compass

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        speed_ms = d.pop("speedMs", UNSET)

        from_deg = d.pop("fromDeg", UNSET)

        compass = d.pop("compass", UNSET)

        sample_value_derived_type_0 = cls(
            speed_ms=speed_ms,
            from_deg=from_deg,
            compass=compass,
        )

        sample_value_derived_type_0.additional_properties = d
        return sample_value_derived_type_0

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
