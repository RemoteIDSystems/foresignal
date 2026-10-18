from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="FieldManifestGrid")


@_attrs_define
class FieldManifestGrid:
    """Regular lat/lon grid; rows north→south, longitudes 0..359

    Attributes:
        nx (int):
        ny (int):
        lo1 (float):
        la1 (float):
        dx (float):
        dy (float):
    """

    nx: int
    ny: int
    lo1: float
    la1: float
    dx: float
    dy: float
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        nx = self.nx

        ny = self.ny

        lo1 = self.lo1

        la1 = self.la1

        dx = self.dx

        dy = self.dy

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "nx": nx,
                "ny": ny,
                "lo1": lo1,
                "la1": la1,
                "dx": dx,
                "dy": dy,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        nx = d.pop("nx")

        ny = d.pop("ny")

        lo1 = d.pop("lo1")

        la1 = d.pop("la1")

        dx = d.pop("dx")

        dy = d.pop("dy")

        field_manifest_grid = cls(
            nx=nx,
            ny=ny,
            lo1=lo1,
            la1=la1,
            dx=dx,
            dy=dy,
        )

        field_manifest_grid.additional_properties = d
        return field_manifest_grid

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
