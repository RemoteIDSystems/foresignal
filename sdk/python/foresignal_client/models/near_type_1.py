from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define

T = TypeVar("T", bound="NearType1")


@_attrs_define
class NearType1:
    """Near a fixed point

    Attributes:
        point (list[float]): [lon, lat]
        radius_km (float):
    """

    point: list[float]
    radius_km: float

    def to_dict(self) -> dict[str, Any]:
        point = self.point

        radius_km = self.radius_km

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "point": point,
                "radiusKm": radius_km,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        point = cast(list[float], d.pop("point"))

        radius_km = d.pop("radiusKm")

        near_type_1 = cls(
            point=point,
            radius_km=radius_km,
        )

        return near_type_1
