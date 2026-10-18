from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SensorCapabilitiesType0")


@_attrs_define
class SensorCapabilitiesType0:
    """Curated public capabilities

    Attributes:
        operator (None | str | Unset):
        sensor_types (list[str] | Unset):
        resolution_m (float | None | Unset):
        swath_km (float | None | Unset):
    """

    operator: None | str | Unset = UNSET
    sensor_types: list[str] | Unset = UNSET
    resolution_m: float | None | Unset = UNSET
    swath_km: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        operator: None | str | Unset
        if isinstance(self.operator, Unset):
            operator = UNSET
        else:
            operator = self.operator

        sensor_types: list[str] | Unset = UNSET
        if not isinstance(self.sensor_types, Unset):
            sensor_types = self.sensor_types

        resolution_m: float | None | Unset
        if isinstance(self.resolution_m, Unset):
            resolution_m = UNSET
        else:
            resolution_m = self.resolution_m

        swath_km: float | None | Unset
        if isinstance(self.swath_km, Unset):
            swath_km = UNSET
        else:
            swath_km = self.swath_km

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if operator is not UNSET:
            field_dict["operator"] = operator
        if sensor_types is not UNSET:
            field_dict["sensorTypes"] = sensor_types
        if resolution_m is not UNSET:
            field_dict["resolutionM"] = resolution_m
        if swath_km is not UNSET:
            field_dict["swathKm"] = swath_km

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_operator(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        operator = _parse_operator(d.pop("operator", UNSET))

        sensor_types = cast(list[str], d.pop("sensorTypes", UNSET))

        def _parse_resolution_m(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        resolution_m = _parse_resolution_m(d.pop("resolutionM", UNSET))

        def _parse_swath_km(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        swath_km = _parse_swath_km(d.pop("swathKm", UNSET))

        sensor_capabilities_type_0 = cls(
            operator=operator,
            sensor_types=sensor_types,
            resolution_m=resolution_m,
            swath_km=swath_km,
        )

        sensor_capabilities_type_0.additional_properties = d
        return sensor_capabilities_type_0

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
